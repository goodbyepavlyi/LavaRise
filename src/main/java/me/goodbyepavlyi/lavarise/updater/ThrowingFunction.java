package me.goodbyepavlyi.lavarise.updater;

@FunctionalInterface
interface ThrowingFunction<T,R,E extends Exception> {
    R apply(T t) throws E;
}