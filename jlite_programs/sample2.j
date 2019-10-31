class Main {
    Void main() {
        Tree set;
        Int i;
        set.setValue(0);
        while (i < 10) {
            set = set.add(i);
            i = i + 1;
        }
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

    Tree add(Int value) {
        Tree newTree;
        if (value == this.value) {
            return this;
        } else {
            newTree = this.copy();
            if (value < this.value) {
                if (isNull(this.left)) {
                    newTree.left = new Tree();
                    newTree.left.setValue(value);
                } else {
                    newTree.left = this.left.add(value);
                }
            } else {
                if (isNull(this.right)) {
                    newTree.right = new Tree();
                    newTree.right.setValue(value);
                } else {
                    newTree.right = this.right.add(value);
                }
            }
            return newTree;
        }
    }
}
