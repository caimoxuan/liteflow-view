package com.cmx.extension.tools;

import java.util.function.Supplier;


/**
 * 延迟加载
 * 在一些流程中，有些数据可能不是左右的流程都会使用，使用。延迟加载可在不适用的场景中避免多余的加载带来的损耗
 * 常用在一些情况下需要请求数据库，将请求的方法放入延迟加载中， 当实际get的时候才会去请求数据库
 * @param <T> 被加载类型
 */
public class LazyInit<T> {

    private final Supplier<T> supplier;

    private LazyInit(Supplier<T> supplier) {
        this.supplier = supplier;
    }

    public static <T> LazyInit<T> of(Supplier<T> supplier) {
        return new LazyInit<>(supplier);
    }

    public T get() {
        return supplier.get();
    }

}
