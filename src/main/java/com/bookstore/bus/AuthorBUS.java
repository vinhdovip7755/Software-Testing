package com.bookstore.bus;

import com.bookstore.dao.AuthorDAO;
import com.bookstore.dto.AuthorDTO;

import java.util.List;

public class AuthorBUS {
    private static final String NAME_REGEX = "^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$";
    private static final int MAX_FIELD_LENGTH = 100;

    private final AuthorDAO authorDAO = new AuthorDAO();

    public List<AuthorDTO> selectAllAuthors() {
        return authorDAO.selectAllAuthors();
    }

    public String addAuthor(AuthorDTO author) {
        if (author == null || author.getAuthorName() == null || author.getAuthorName().trim().isEmpty())
            return "Tên tác giả không được để trống!";
        String trimmedName = author.getAuthorName().trim();
        if (trimmedName.length() > MAX_FIELD_LENGTH || !trimmedName.matches(NAME_REGEX))
            return "Tên tác giả không hợp lệ!";
        if (authorDAO.exists(trimmedName))
            return "Tên tác giả đã tồn tại trong hệ thống!";
        if (author.getNationality() == null || author.getNationality().trim().isEmpty())
            return "Quốc tịch tác giả không được để trống!";
        String trimmedNationality = author.getNationality().trim();
        if (trimmedNationality.length() > MAX_FIELD_LENGTH || !trimmedNationality.matches(NAME_REGEX))
            return "Quốc tịch tác giả không hợp lệ!";

        author.setAuthorName(trimmedName);
        author.setNationality(trimmedNationality);
        int id = authorDAO.add(author);

        if (id != -1) {
            return "Thêm tác giả thành công!";
        }
        return "Thêm tác giả thất bại!";
    }

    public String updateAuthor(AuthorDTO author) {
        if (author == null || author.getAuthorName() == null || author.getAuthorName().trim().isEmpty())
            return "Tên tác giả không được để trống!";
        String trimmedName = author.getAuthorName().trim();
        if (trimmedName.length() > MAX_FIELD_LENGTH || !trimmedName.matches(NAME_REGEX))
            return "Tên tác giả không hợp lệ!";
        if (author.getNationality() == null || author.getNationality().trim().isEmpty())
            return "Quốc tịch tác giả không được để trống!";
        String trimmedNationality = author.getNationality().trim();
        if (trimmedNationality.length() > MAX_FIELD_LENGTH || !trimmedNationality.matches(NAME_REGEX))
            return "Quốc tịch tác giả không hợp lệ!";

        author.setAuthorName(trimmedName);
        author.setNationality(trimmedNationality);
        if (authorDAO.update(author)) {
            return "Cập nhật tác giả thành công!";
        }
        return "Cập nhật tác giả thất bại!";
    }
}
