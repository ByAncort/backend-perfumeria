package com.app.productos.Models;

import java.util.ArrayList;
import java.util.List;

public class ServiceResult<T> {

    private T data;
    private List<String> errors;

    public ServiceResult(T data) {
        this.data = data;
        this.errors = new ArrayList<>();
    }

    public ServiceResult(List<String> errors) {
        this.errors = errors;
        this.data = null;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public T getData() {
        return data;
    }

    public List<String> getErrors() {
        return errors;
    }
}
