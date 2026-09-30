package com.bookstore.bus;
import com.bookstore.dao.ProductStatsDAO;
import com.bookstore.dto.ProductStatsDTO;

import java.sql.Date;
import java.util.List;

public class ProductStatsBUS {
    private final ProductStatsDAO dao = new ProductStatsDAO();

    public List<ProductStatsDTO> getThongKeTheoNgay(Date tuNgay, Date denNgay){return dao.getThongKeTheoNgay(tuNgay, denNgay);}

    public List<ProductStatsDTO> getThongKeTheoThang(int year){return dao.getThongKeTheoThang(year);}

    public List<ProductStatsDTO> getThongKeTheoQuy(int year){return dao.getThongKeTheoQuy(year);}

    public List<ProductStatsDTO> getThongKeTheoNam(){return dao.getThongKeTheoNam();}

    public List<ProductStatsDTO> getTop3BanChayTheoQuy(int year, int quarter){return dao.getTop3BanChayTheoQuy(year, quarter);}

    public List<ProductStatsDTO> getTop3BanItTheoQuy(int year, int quarter){return dao.getTop3BanItTheoQuy(year, quarter);}
}