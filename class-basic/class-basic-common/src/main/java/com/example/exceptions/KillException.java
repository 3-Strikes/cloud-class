package com.example.exceptions;

import com.example.enums.E;

public class KillException extends RuntimeException{
    private E e;

    public KillException(E e) {
        this.e = e;
    }

    public E getE() {
        return e;
    }

    public void setE(E e) {
        this.e = e;
    }
}
