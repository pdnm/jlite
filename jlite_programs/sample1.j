class Main {
    Void main(String arg) {
        Maths maths;
        Int a;
        Int b;
        maths = new Maths();
        maths.self().self().modulo = 1000000007;
        readln(a);
        readln(b);
        println(maths.gcd(a, b));
        println(maths.pow(2, 32) - 1);
    }
}

class Maths {
    Int modulo;

    Maths self() {
        return this;
    }

    Int gcd(Int a, Int b) {
        if (b == 0) {
            return a;
        } else {
            return gcd(b, mod(a, b));
        }
    }

    Int mod(Int a, Int b) {
        return a - a / b * b;
    }

    Int pow(Int a, Int n) {
        Int res;
        res = 1;
        while (n != 0) {
            if (mod(n, 2) == 1) {
                res = mod(res * a, modulo);
            } else {
                res = res * 1;
            }
            a = mod(a * a, modulo);
            n = n / 2;
        }
        return res;
    }
}