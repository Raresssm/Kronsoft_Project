package com.agora.agoracampus.exception;

public class InternshipNotFoundException extends RuntimeException {

    public InternshipNotFoundException(Long id) {
        super("Internship-ul cu id-ul " + id + " nu a fost gasit.");
    }
}
