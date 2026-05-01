package com.agora.agoracampus.exception;

public class CompetitionNotFoundException extends RuntimeException {

    public CompetitionNotFoundException(Long id) {
        super("Competitia cu id-ul " + id + " nu a fost gasita.");
    }
}
