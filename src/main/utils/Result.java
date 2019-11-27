package utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.*;

public abstract class Result<T, E> {
    private static class Success<T, E> extends Result<T, E> {
        private final T value;

        private Success(T value) {
            this.value = value;
        }

        @Override
        public <R> Result<R, E> map(Function<T, R> f) {
            return new Success<>(f.apply(value));
        }

        @Override
        public <R> Result<R, E> flatMap(Function<T, Result<R, E>> f) {
            return f.apply(value);
        }

        @Override
        public <R> R fold(Function<T, R> ifSuccess, Function<E, R> ifError) {
            return ifSuccess.apply(value);
        }
    }

    private static class Error<T, E> extends Result<T, E> {
        private final E value;

        private Error(E value) {
            this.value = value;
        }

        @Override
        public <R> Result<R, E> map(Function<T, R> f) {
            return new Error<>(value);
        }

        @Override
        public <R> Result<R, E> flatMap(Function<T, Result<R, E>> f) {
            return new Error<>(value);
        }

        @Override
        public <R> R fold(Function<T, R> ifSuccess, Function<E, R> ifError) {
            return ifError.apply(value);
        }
    }

    public abstract <R> R fold(Function<T, R> ifSuccess, Function<E, R> ifError);
    public void consume(Consumer<T> ifSuccess, Consumer<E> ifError) {
        fold(
                x -> {
                    ifSuccess.accept(x);
                    return true;
                },
                y -> {
                    ifError.accept(y);
                    return true;
                });
    }
    public abstract <R> Result<R, E> map(Function<T, R> f);
    public abstract <R> Result<R, E> flatMap(Function<T, Result<R, E>> f);

    public static <T1, T2, R, E> Result<R, E> map2(Result<T1, E> r1, Result<T2, E> r2, BiFunction<T1, T2, R> f) {
        return r1.flatMap(t1 -> r2.map(t2 -> f.apply(t1, t2)));
    }

    public static <T, R, E> Result<List<R>, E> traverseAll(List<T> xs, Function<T, Result<R, E>> f, BinaryOperator<E> combine) {
        var result = new ArrayList<R>();
        var errs = new ArrayList<E>();
        for (var x : xs) {
            f.apply(x).consume(result::add, errs::add);
        }
        if (errs.isEmpty()) return success(result);
        else return error(errs.stream().reduce(combine).get());
    }

    public static <T, E> Result<T, E> flatten(Result<Result<T, E>, E> result) {
        return result.flatMap(x -> x);
    }

    public static <T, E> Result<T, E> success(T value) {
        return new Success<>(value);
    }

    public static <T, E> Result<T, E> error(E value) {
        return new Error<>(value);
    }
}
