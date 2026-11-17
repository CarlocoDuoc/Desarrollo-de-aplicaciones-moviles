package com.esucar.api.web;

import com.esucar.api.web.Dtos.ErrorRes;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ManejadorErrores {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorRes> estado(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(new ErrorRes(e.getReason()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorRes> validacion(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(f -> f.getField() + ": " + f.getDefaultMessage())
                .orElse("Datos inválidos");
        return ResponseEntity.badRequest().body(new ErrorRes(msg));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorRes> cuerpo(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(new ErrorRes("Cuerpo de la solicitud inválido"));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorRes> otro(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorRes("Error interno del servidor"));
    }
}
