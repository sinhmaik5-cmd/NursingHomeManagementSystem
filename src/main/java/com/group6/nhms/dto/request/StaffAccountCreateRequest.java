package com.group6.nhms.dto.request;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public class StaffAccountCreateRequest {


    @NotBlank(message = "Vui lòng nhập họ và tên nhân viên")
    @Size(max = 100, message = "Họ và tên không quá 100 ký tự")
    private String fullName;


    @NotBlank(message = "Vui lòng chọn chức danh / vai trò")
    private String jobTitle; // DOCTOR, NURSE, RECEPTIONIST, CENTER_MANAGER


    @NotBlank(message = "Vui lòng nhập số điện thoại")
    @Size(max = 20, message = "Số điện thoại không quá 20 ký tự")
    private String phone;


    @Size(max = 100, message = "Email không quá 100 ký tự")
    private String email;


    @Size(max = 20, message = "Căn cước công dân không quá 20 ký tự")
    private String citizenId;


    private String specialization;


    @NotBlank(message = "Vui lòng nhập tên đăng nhập")
    @Size(min = 3, max = 50, message = "Tên đăng nhập từ 3 đến 50 ký tự")
    private String username;


    @NotBlank(message = "Vui lòng nhập mật khẩu")
    @Size(min = 6, message = "Mật khẩu phải từ 6 ký tự trở lên")
    private String password;


    @NotBlank(message = "Vui lòng xác nhận mật khẩu")
    private String confirmPassword;


    public StaffAccountCreateRequest() {
    }


    public String getFullName() {
        return fullName;
    }


    public void setFullName(String fullName) {
        this.fullName = fullName;
    }


    public String getJobTitle() {
        return jobTitle;
    }


    public void setJobTitle(String jobTitle) {
        this.jobTitle = jobTitle;
    }


    public String getPhone() {
        return phone;
    }


    public void setPhone(String phone) {
        this.phone = phone;
    }


    public String getEmail() {
        return email;
    }


    public void setEmail(String email) {
        this.email = email;
    }


    public String getCitizenId() {
        return citizenId;
    }


    public void setCitizenId(String citizenId) {
        this.citizenId = citizenId;
    }


    public String getSpecialization() {
        return specialization;
    }


    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }


    public String getUsername() {
        return username;
    }


    public void setUsername(String username) {
        this.username = username;
    }


    public String getPassword() {
        return password;
    }


    public void setPassword(String password) {
        this.password = password;
    }


    public String getConfirmPassword() {
        return confirmPassword;
    }


    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}

