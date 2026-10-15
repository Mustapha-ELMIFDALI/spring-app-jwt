package jwtapp.dtos;

public record Loginrequest(
        String email,
        String password
) {
}
