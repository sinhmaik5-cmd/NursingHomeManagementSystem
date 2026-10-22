package com.group6.nhms.enums;

public enum RoleEnum {
    ADMIN,      // Quản trị viên hệ thống
    DOCTOR,     // Bác sĩ (Khám bệnh, chỉ số sinh tồn, kê đơn thuốc)
    NURSE,      // Điều dưỡng (Chăm sóc, đo sinh hiệu, sự cố, trực ca, dọn giường)
    MANAGER,    // Quản lý (Tiếp nhận cư dân, xếp phòng, chuyển phòng, phân công ca trực)
    ACCOUNTANT, // Kế toán (Tạo hóa đơn, chốt billing, xác nhận thanh toán)
    RELATIVE,   // Người thân/Người bảo lãnh
    STAFF       // Nhân viên chung (dành cho cấp quyền chung hoặc tương thích ngược)
}
