type symbol = [`Nonterm of char | `Term of char]
type term = [`Term of char]
type nonterm = [`Nonterm of char]
type prod = Prod of nonterm * (symbol list)
type grammar = Grammar of
  {start: nonterm; terms: term list; nonterms: nonterm list; prods: prod list}

module TermSet = Set.Make (struct type t = term let compare = compare end)

type item = Item of prod * int * TermSet.t

module ItemSet = Set.Make (struct type t = item let compare = compare end)


let rec insert i x xs =
  if i = 0 then x :: xs
  else (List.hd xs) :: insert (i - 1) x (List.tl xs)

let rec drop n = function
  | [] -> []
  | (_ :: xs) as list -> if n = 0 then list else drop (n - 1) xs

let index x xs =
  let rec aux i = function
  | [] -> -1
  | hd :: tl -> if hd = x then i else aux (i + 1) tl
  in aux 0 xs


let symbol_to_char = function
  | `Term t -> t
  | `Nonterm nt -> nt

let item_to_string (Item (Prod (`Nonterm lhs, rhs), pos, look_aheads)) =
  Printf.sprintf "(%c -> %s, %s)"
    lhs
    (rhs |> List.map symbol_to_char |> insert pos '*' |> List.to_seq
      |> String.of_seq) 
    (look_aheads |> TermSet.to_seq |> Seq.map symbol_to_char |> String.of_seq)

let item_set_to_string set =
  "{" ^
  (set |> ItemSet.to_seq |> Seq.map item_to_string |> List.of_seq
    |> String.concat ", ")
   ^ "}"

let first (Grammar grammar) syms =
  let result = Hashtbl.create 4 in
  let marked = Hashtbl.create 4 in
  let queue = Queue.create () in
  Queue.add syms queue; Hashtbl.add marked syms ();
  while not (Queue.is_empty queue) do
    let syms = Queue.pop queue in
    match syms with
    | [] -> ()
    | (`Term _ as t) :: _ -> Hashtbl.add result t ()
    | (`Nonterm _ as nt) :: _ -> begin
      grammar.prods |> List.iter (fun (Prod (nt', syms')) ->
        if nt' = nt && Hashtbl.find_opt marked syms' = None then begin
          Hashtbl.add marked syms' ();
          Queue.add syms' queue;
        end
      )
    end
  done;
  result |> Hashtbl.to_seq_keys |> List.of_seq

let closure (Grammar grammar as gr) set =
  let marked = Hashtbl.create 4 in
  let has_item (prod, pos, t) =
    match Hashtbl.find_opt marked (prod, pos) with
    | Some terms -> TermSet.mem t terms
    | None -> false in
  let add_item (prod, pos, t) =
    match Hashtbl.find_opt marked (prod, pos) with
    | Some terms -> Hashtbl.replace marked (prod, pos) (TermSet.add t terms)
    | None -> Hashtbl.add marked (prod, pos) (TermSet.singleton t) in

  let queue = Queue.create () in
  set |> ItemSet.iter (fun (Item (prod, pos, look_aheads)) ->
    look_aheads |> TermSet.iter (fun t ->
      Queue.add (prod, pos, t) queue;
      add_item (prod, pos, t)
    )
  );
  while not (Queue.is_empty queue) do
    let ((Prod (_, rhs)), pos, la) = Queue.pop queue in
    if pos < List.length rhs then
      match List.nth rhs pos with
      | `Nonterm _ as nt ->
        let rem = drop (pos + 1) rhs in
        let look_aheads = first gr (rem @ [(la :> symbol)]) in
        grammar.prods |> List.iter (fun (Prod (nt', _) as prod) ->
          if nt' = nt then begin
            look_aheads |> List.iter (fun t ->
              let new_item = (prod, 0, t) in
              if not (has_item new_item) then begin
                Queue.add new_item queue;
                add_item new_item
              end
            )
          end
        )
      | `Term _ -> ()
  done;

  marked 
    |> Hashtbl.to_seq
    |> Seq.map (fun ((prod, pos), terms) -> Item (prod, pos, terms))
    |> ItemSet.of_seq

let goto grammar set sym =
  let kernel = set
    |> ItemSet.filter (fun (Item (Prod (_, rhs), pos, _)) ->
      pos < List.length rhs && List.nth rhs pos = sym)
    |> ItemSet.map (fun (Item (prod, pos, terms)) ->
      Item (prod, pos + 1, terms)) in
  (kernel, closure grammar kernel)

let grammar_symbols (Grammar grammar) =
  List.map (fun t -> (t :> symbol)) grammar.terms
        @ List.map (fun t -> (t :> symbol)) grammar.nonterms

let lr1_states (Grammar grammar as gr) st eof =
  let marked = Hashtbl.create 4 in
  let queue = Queue.create () in
  let aux_prod = Prod (`Nonterm st, [(grammar.start :> symbol)]) in
  let start_item = Item (aux_prod,
                        0, 
                        TermSet.singleton (`Term eof)) in
  let source = ItemSet.singleton start_item
                |> closure gr in
  Queue.add source queue;
  Hashtbl.add marked source 0;

  Printf.printf "start --> CLOSURE({%s})\n\t = I%d := %s\n"
    (item_to_string start_item)
    0 (item_set_to_string source);

  while not (Queue.is_empty queue) do
    let set = Queue.pop queue in
    let set_id = Hashtbl.find marked set in
    grammar_symbols gr
      |> List.iter (fun x ->
        let (kernel, set') = goto gr set x in
        if not (ItemSet.is_empty set') && Hashtbl.find_opt marked set' = None
        then begin
          let set'_id = (Hashtbl.length marked) in

          Printf.printf "I%d --%c--> CLOSURE(%s)\n\t = I%d := %s\n"
            set_id
            (symbol_to_char x)
            (item_set_to_string kernel)
            set'_id (item_set_to_string set');

          Queue.add set' queue;
          Hashtbl.add marked set' set'_id
        end
      )
  done;
  marked |> Hashtbl.to_seq |> List.of_seq

let canonical_core set =
  set
    |> ItemSet.to_seq
    |> Seq.map (fun (Item (prod, pos, _)) -> (prod, pos))
    |> List.of_seq |> List.sort compare

let lalr_states grammar st eof =
  let states = lr1_states grammar st eof in
  let merge set1 set2 =
    set1
      |> ItemSet.map (fun (Item (prod, pos, la1)) ->
        let Item (_, _, la2) =
          set2
            |> ItemSet.find_first (fun (Item (prod', pos', _)) ->
              (prod', pos') >= (prod, pos)) in
        Item (prod, pos, TermSet.union la1 la2)
      ) in
  let merged_states = Hashtbl.create 4 in
  states
    |> List.iter (fun (set, id) ->
      let core = canonical_core set in
      match Hashtbl.find_opt merged_states core with
      | Some (ids, merged) ->
        Hashtbl.replace merged_states core (id :: ids, merge merged set)
      | None ->
        Hashtbl.add merged_states core ([id], set)
    );
  merged_states |> Hashtbl.to_seq_values |> List.of_seq |> List.sort compare

let parse_table (Grammar grammar as gr) states =
  let state_id =
    states
      |> List.mapi (fun i (_, set) -> (canonical_core set, i))
      |> List.to_seq |> Hashtbl.of_seq in
  let goto_transitions =
    states
      |> List.fold_left (fun acc (_, set) ->
        let i = Hashtbl.find state_id (canonical_core set) in
        grammar_symbols gr |> List.fold_left (fun acc sym ->
          let core = canonical_core (snd (goto gr set sym)) in
          if core = [] then acc
          else (i, (symbol_to_char sym), Hashtbl.find state_id core) :: acc
        ) acc
      ) []
      |> List.rev in
  let reduce_transitions = 
    states
      |> List.fold_left (fun acc (_, set) ->
        let i = Hashtbl.find state_id (canonical_core set) in
        ItemSet.fold (fun (Item (Prod (_, rhs) as prod, pos, las)) acc ->
          if pos = (List.length rhs) then
            (las
                |> TermSet.to_seq
                |> List.of_seq
                |> List.map (fun t ->
                  (i, (symbol_to_char t), (index prod grammar.prods))))
              @ acc
          else acc
        ) set acc
      ) []
      |> List.rev in
  (goto_transitions, reduce_transitions)

let test () =
  let terms = ['s'; 'l'; 'r'; 'm'; 'n'] |> List.map (fun t -> `Term t) in
  let nonterms = ['S'; 'L'; 'P'; 'E'] |> List.map (fun nt -> `Nonterm nt) in
  let parse_prod (lhs, rhs) =
    Prod (`Nonterm lhs, rhs |> String.to_seq |> List.of_seq |>
      List.map (fun c ->
        if Char.lowercase_ascii c = c then `Term c else `Nonterm c 
      )) in
  let prods = [
    ('L', "LP");
    ('L', "P");
    ('P', "Es");
    ('E', "EmE");
    ('E', "lEr");
    ('E', "mE");
    ('E', "n");
  ] |> List.map parse_prod in
  let grammar = Grammar {start = `Nonterm 'L'; terms; nonterms; prods} in

  let states = lalr_states grammar 'S' '$' in
  Printf.printf "\nThere are %d LALR(1) states:\n" (List.length states);
  states |> List.iteri (fun i (ids, set)
      -> Printf.printf "#%d = I{%s} = %s\n"
          i
          (ids |> List.map string_of_int |> String.concat ",")
          (item_set_to_string set));
  Printf.printf "\n";
  
  let (gotos, reduces) = parse_table grammar states in
  gotos
    |> List.iter (fun (i, c, j) ->
      Printf.printf "#%d --%c--> #%d\n" i c j);
  Printf.printf "\n";
  reduces
    |> List.iter (fun (i, c, p) ->
      Printf.printf "#%d at %c reduce p%d\n" i c p)

let () = test ()
