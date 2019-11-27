class Main {
    Int main() {
        Tree set;
        Int i;

        set = new Tree();
        set.setValue(0);

        i = 0;
        while (i < 10) {
            set = set.add(mod(i * i, 10));
            i = i + 1;
        }
        println(set.size());
        return 0;
    }

    Int div(Int a, Int b) {
        Int res;
        res = 0;
        while (a >= b) {
            a = a - b;
            res = res + 1;
        }
        return res;
    }

    Int mod(Int a, Int b) {
        return a - div(a, b) * b;
    }
}

class Tree {
    Int value;
    Tree left;
    Tree right;

    Void setValue(Int value) {
        this.value = value;
    }

    Tree copy() {
        Tree newTree;
        newTree = new Tree();
        newTree.value = value;
        newTree.left = left;
        newTree.right = right;
        return newTree;
    }

    Int size() {
        if (isNull(this)) {
            return 0;
        } else {
            return 1 + left.size() + right.size();
        }
    }

    Tree add(Int value) {
        Tree newTree;
        if (isNull(this)) {
            newTree = new Tree();
            newTree.setValue(value);
            return newTree;
        } else {
            if (value == this.value) {
                return this;
            } else {
                newTree = this.copy();
                if (value < this.value) {
                    newTree.left = newTree.left.add(value);
                } else {
                    newTree.right = newTree.right.add(value);
                }
                return newTree;
            }
        }
    }
}
