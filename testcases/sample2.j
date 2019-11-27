class Main {
    Int fibonacci(Int n) {
        if (n < 2) {
            return n;
        } else {
            return fibonacci(n - 1) + fibonacci(n - 2);
        }
    }

    Int factorial(Int n) {
        if (n == 0) {
            return 1;
        } else {
            return n * factorial(n - 1);
        }
    }

    Int main() {
        Int i;
        println(factorial(10));

        i = 0;
        while (i < 10) {
            println(fibonacci(i));
            i = i + 1;
        }
        return 0;
    }
}