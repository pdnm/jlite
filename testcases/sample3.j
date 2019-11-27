class Main {
    Int main(String arg) {
        Maths maths;
        maths = new Maths();
        maths.modulo = 1000000007;
        println(maths.self().self().modulo);
        println("gcd(34, 55) = "); println(maths.gcd(34, 55));
        println("2 ^ 30 = "); println(maths.pow(2, 20));
        return 0;
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

    Int pow(Int a, Int n) {
        Int res;
        res = 1;
        while (n != 0) {
            if (mod(n, 2) == 1) {
                res = res * a;
            } else {
                res = res * 1;
            }
            a = a * a;
            n = div(n, 2);
        }
        return res;
    }
}
