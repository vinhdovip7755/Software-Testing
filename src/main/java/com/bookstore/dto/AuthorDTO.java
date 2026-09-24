package com.bookstore.dto;

public class AuthorDTO {
    private int authorId;
    private String authorName;
    private String nationality;

    public AuthorDTO() {}

    public AuthorDTO(int authorId, String authorName, String nationality) {
        this.authorId = authorId;
        this.authorName = authorName;
        this.nationality = nationality;
    }

    public int getAuthorId() {
        return authorId;
    }

    public void setAuthorId(int authorId) {
        this.authorId = authorId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    @Override
    public String toString() {
        return this.authorName;
    }
}