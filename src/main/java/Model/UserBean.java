package Model;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class UserBean {

    private int id;
    private String name, surname, email, password, phone, city, province, postalCode, address, state, admin;
    private GregorianCalendar birthday, register;

    public UserBean() {}

    public int getId() {

        return id;
    }

    public void setId(int id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getSurname() {

        return surname;
    }

    public void setSurname(String surname) {

        this.surname = surname;
    }

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = email;
    }

    public String getPassword() {

        return password;
    }

    public void setPassword(String password) {

        try {

            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            digest.reset();
            digest.update(password.getBytes(StandardCharsets.UTF_8));
            this.password = String.format("%040x", new BigInteger(1, digest.digest()));

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public String getPhone() {

        return phone;
    }

    public void setPhone(String phone) {

        this.phone = phone;
    }

    public String getCity() {

        return city;
    }

    public void setCity(String city) {

        this.city = city;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {

        this.province = province;
    }

    public String getPostalCode() {

        return postalCode;
    }

    public void setPostalCode(String postalCode) {

        this.postalCode = postalCode;
    }

    public String getAddress() {

        return address;
    }

    public void setAddress(String address) {

        this.address = address;
    }

    public String getBirthday() {

        return birthday.get(Calendar.YEAR) + "-"
                + ((birthday.get(Calendar.MONTH) + 1)) + "-"
                + (birthday.get(Calendar.DAY_OF_MONTH));
    }

    public void setBirthday(String date) {

        String[] dateSplit = date.split("-");

        int year = Integer.parseInt(dateSplit[0]);
        int mouth = Integer.parseInt(dateSplit[1]);
        int day = Integer.parseInt(dateSplit[2]);

        mouth--;

        this.birthday = new GregorianCalendar(year, mouth, day);
    }

    public String getRegister() {

        return register.get(Calendar.YEAR) + "-"
                + ((register.get(Calendar.MONTH) + 1)) + "-"
                + (register.get(Calendar.DAY_OF_MONTH));
    }

    public void setRegister(String date) {

        String[] dateSplit = date.split("-");

        int year = Integer.parseInt(dateSplit[0]);
        int mouth = Integer.parseInt(dateSplit[1]);
        int day = Integer.parseInt(dateSplit[2]);

        mouth--;

        this.register = new GregorianCalendar(year, mouth, day);
    }

    public void setRegister() {

        this.register = new GregorianCalendar();
    }

    public String isActive() {

        return state;
    }

    public void setState(String state) {

        this.state = state;
    }

    public String isAdmin() {

        return admin;
    }

    public void setAdmin(String admin) {

        this.admin = admin;
    }
}