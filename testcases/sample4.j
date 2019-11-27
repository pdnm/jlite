class Main {
    Int main() {
        List xs;
        Int i;
        i = 0;
        xs = new List();
        xs.value = 0;
        while (i < 5) {
            i = i + 1;
            xs = xs.prepend(i);
        }
        xs.print();
        return 0;
    }
}

class List {
    Int value;
    List next;

    List prepend(Int x) {
        List res;
        res = new List();
        res.value = x;
        res.next = this;
        return res;
    }

    Void print() {
        if (isNull(this)) {
            return;
        } else {
            println(value);
            next.print();
        }
    }
}
