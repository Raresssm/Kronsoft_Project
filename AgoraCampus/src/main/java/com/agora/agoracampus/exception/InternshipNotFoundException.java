package com.agora.agoracampus.exception;

public class InternshipNotFoundException extends RuntimeException {

    public InternshipNotFoundException(Integer id) {
        super("Internship-ul cu id-ul " + id + " nu a fost gasit.");
    }
}
