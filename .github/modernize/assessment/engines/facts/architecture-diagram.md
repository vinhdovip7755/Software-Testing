# Architecture Diagram

Ứng dụng là desktop application Java Swing cho cửa hàng sách, tổ chức theo mô hình ba tầng GUI - BUS - DAO và kết nối cơ sở dữ liệu MySQL. Các nghiệp vụ chính gồm bán hàng, nhập hàng, quản lý tồn kho theo lô, phân quyền và thống kê.

## Application Architecture

<!-- mermaid-checked: no \n, no em-dash/en-dash, no {} in labels, subgraphs are id["label"], arrows are -->|"label"|, all subgraphs closed by end, ids unique -->
~~~mermaid
flowchart TD
    subgraph Client["Desktop Client"]
        Login["LoginFrame"]
        Main["MainFrame"]
        Panels["Business panels"]
    end
    subgraph App["Application Layer - Java Swing"]
        BUS["BUS services"]
        DTO["DTO models"]
        Util["Utilities and exporters"]
    end
    subgraph Data["Data Layer"]
        DAO["DAO repositories"]
        DB[("MySQL bookstore_db")]
    end
    subgraph External["External Actors"]
        Customer["Customer"]
        Supplier["Supplier"]
        Staff["Store staff"]
    end

    Customer -->|"purchase request"| Panels
    Supplier -->|"delivery documents"| Panels
    Staff -->|"uses desktop UI"| Login
    Login --> Main --> Panels
    Panels -->|"delegates"| BUS
    BUS -->|"maps data"| DTO
    BUS -->|"queries"| DAO
    DAO -->|"JDBC SQL"| DB
    BUS -->|"exports"| Util
~~~

### Technology Stack Summary

| Layer | Technology | Version | Purpose |
|---|---|---|---|
| Client | Java Swing | JDK 17 | Desktop user interface |
| UI theme | FlatLaf | 3.5.2 | Modern Swing look and feel |
| Business | Java BUS classes | JDK 17 | Validation and business rules |
| Data access | JDBC and MySQL Connector/J | 8.3.0 | Database persistence |
| Database | MySQL `bookstore_db` | Not specified | Books, lots, bills, imports and permissions |
| Export | iTextPDF and Apache POI | 5.5.13.3 and 5.4.0 | PDF invoices and Excel reports |

### Data Storage & External Services

MySQL stores the normalized bookstore data, including books, book lots, bills, import tickets, customers, employees, roles, permissions, prices and promotions. The application does not show a remote API or message broker; external interaction is represented by customers, suppliers and generated PDF or Excel files. `book_lot` and `inventory_log` support lot-level stock tracking and inventory history.

### Key Architectural Decisions

- Uses a three-layer desktop architecture: Swing panels call BUS classes, which call DAO classes.
- Uses DTO objects to transfer database records between the data and presentation layers.
- Uses status fields instead of physical deletion for important business records such as bills, imports, books and accounts.

## Component Relationships

<!-- mermaid-checked: no \n, no em-dash/en-dash, no {} in labels, subgraphs are id["label"], arrows are -->|"label"|, all subgraphs closed by end, ids unique -->
~~~mermaid
flowchart LR
    subgraph PresentationLayer["Presentation"]
        cLogin["LoginFrame"]
        cMain["MainFrame"]
        cSell["SellingPanel"]
        cImport["ImportPanel"]
        cProduct["ProductPanel"]
        cStats["Statistics panels"]
    end
    subgraph BusinessLayer["Business Logic"]
        cAccountBUS["AccountBUS"]
        cBillBUS["BillBUS"]
        cImportBUS["ImportBUS"]
        cBookBUS["BookBUS"]
        cCustomerBUS["CustomerBUS"]
        cStatsBUS["Statistics BUS"]
    end
    subgraph DataAccessLayer["Data Access"]
        cAccountDAO["AccountDAO"]
        cBillDAO["BillDAO"]
        cImportDAO["ImportDAO"]
        cBookDAO["BookDAO"]
        cCustomerDAO["CustomerDAO"]
        cStatsDAO["Statistics DAO"]
    end
    subgraph InfraLayer["Infrastructure"]
        cDatabase["DatabaseConnection"]
        cPdf["BillPDFExporter"]
        cExcel["ExcelUtil"]
        cPermission["PermissionUtil"]
    end

    cLogin -->|"authenticates"| cAccountBUS
    cMain -->|"hosts panels"| cSell
    cMain -->|"hosts panels"| cImport
    cMain -->|"hosts panels"| cProduct
    cMain -->|"hosts panels"| cStats
    cSell -->|"processes sales"| cBillBUS
    cSell -->|"looks up books"| cBookBUS
    cSell -->|"updates customers"| cCustomerBUS
    cImport -->|"processes imports"| cImportBUS
    cProduct -->|"manages books"| cBookBUS
    cStats -->|"loads reports"| cStatsBUS
    cAccountBUS -->|"uses"| cAccountDAO
    cBillBUS -->|"uses"| cBillDAO
    cImportBUS -->|"uses"| cImportDAO
    cBookBUS -->|"uses"| cBookDAO
    cCustomerBUS -->|"uses"| cCustomerDAO
    cStatsBUS -->|"uses"| cStatsDAO
    cAccountDAO -->|"opens connection"| cDatabase
    cBillDAO -->|"opens connection"| cDatabase
    cImportDAO -->|"opens connection"| cDatabase
    cBookDAO -->|"opens connection"| cDatabase
    cCustomerDAO -->|"opens connection"| cDatabase
    cStatsDAO -->|"opens connection"| cDatabase
    cBillBUS -.->|"exports invoice"| cPdf
    cStatsBUS -.->|"exports report"| cExcel
    cPermission -.->|"checks access"| cMain
~~~

### Component Inventory

| Component | Layer | Type | Responsibility |
|---|---|---|---|
| `LoginFrame` | Presentation | JFrame | Login and entry point |
| `MainFrame` | Presentation | JFrame | Hosts modules after authentication |
| `SellingPanel` | Presentation | JPanel | Search books and create sales bills |
| `ImportPanel` | Presentation | JPanel | Create and manage import tickets |
| `ProductPanel` | Presentation | JPanel | Manage books, categories, authors and suppliers |
| `Statistics panels` | Presentation | JPanel | Show customer, product and financial statistics |
| `BillBUS` | Business | Service | Validate and process bills |
| `ImportBUS` | Business | Service | Process import tickets and stock updates |
| `BookBUS` | Business | Service | Manage books and book lots |
| `AccountBUS` | Business | Service | Authenticate accounts |
| `CustomerBUS` | Business | Service | Manage customers and membership points |
| `Statistics BUS` | Business | Service | Aggregate business statistics |
| `*DAO` classes | Data Access | Repository | Execute SQL and map records |
| `DatabaseConnection` | Infrastructure | Connection helper | Create MySQL connections |
| `BillPDFExporter` | Infrastructure | Exporter | Generate invoice PDF |
| `ExcelUtil` | Infrastructure | Exporter | Generate Excel reports |
| `PermissionUtil` | Infrastructure | Security helper | Check role permissions |
