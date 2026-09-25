package com.bookstore.bus;

import com.bookstore.dao.AuthorDAO;
import com.bookstore.dto.AuthorDTO;

import java.util.List;

public class AuthorBUS {
    private final AuthorDAO authorDAO = new AuthorDAO();

    public List<AuthorDTO> selectAllAuthors() {
        return authorDAO.selectAllAuthors();
    }

    public String addAuthor(AuthorDTO author) {
        if (author.getAuthorName().trim().isEmpty())
            return "Tên tác giả không được để trống!";
        if (!author.getAuthorName().trim().matches("^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$"))
            return "Tên tác giả không hợp lệ!";
        if (authorDAO.exists(author.getAuthorName()))
            return "Tên tác giả đã tồn tại trong hệ thống!";
        if (author.getNationality().trim().isEmpty())
            return "Quốc tịch tác giả không được để trống!";
        if (!author.getNationality().trim().matches("^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$"))
            return "Quốc tịch tác giả không hợp lệ!";

        int id = authorDAO.add(author);

        if (id != -1) {
            return "Thêm tác giả thành công!";
        }
        return "Thêm tác giả thất bại!";
    }

    public String updateAuthor(AuthorDTO author) {
        if (author.getAuthorName().trim().isEmpty())
            return "Tên tác giả không được để trống!";
        if (!author.getAuthorName().trim().matches("^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$"))
            return "Tên tác giả không hợp lệ!";
        if (author.getNationality().trim().isEmpty())
            return "Quốc tịch tác giả không được để trống!";
        if (!author.getNationality().trim().matches("^(?=.*\\p{L})[\\p{L}\\s.'\\u2019\\u2018\\u0060\\u02BB\\u2013-]+$"))
            return "Quốc tịch tác giả không hợp lệ!";
        if (authorDAO.update(author)) {
            return "Cập nhật tác giả thành công!";
        }
        return "Cập nhật tác giả thất bại!";
    }
}
