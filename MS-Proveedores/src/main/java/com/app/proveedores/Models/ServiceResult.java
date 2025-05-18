package com.app.proveedores.Models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
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
}