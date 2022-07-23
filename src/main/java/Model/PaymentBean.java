package Model;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class PaymentBean {

    private int order;
    private String cardNumber, CVV, holder;
    private GregorianCalendar datePayment, deadline;

    public PaymentBean() {}

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public String getDatePayment() {

        return datePayment.get(Calendar.YEAR) + "-"
                + ((datePayment.get(Calendar.MONTH) + 1)) + "-"
                + (datePayment.get(Calendar.DAY_OF_MONTH));
    }

    public void setDatePayment(String datePayment) {

        String[] dateSplit = datePayment.split("-");

        int year = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]);
        int day = Integer.parseInt(dateSplit[2]);

        month--;

        this.datePayment = new GregorianCalendar(year, month, day);
    }

    public void setDatePayment() {

        this.datePayment = new GregorianCalendar();
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public String getCVV() {
        return CVV;
    }

    public void setCVV(String CVV) {
        this.CVV = CVV;
    }

    public String getDeadline() {
        return deadline.get(Calendar.YEAR) + "-"
                + ((deadline.get(Calendar.MONTH) + 1)) + "-"
                + (deadline.get(Calendar.DAY_OF_MONTH));
    }

    public void setDeadline(String deadline) {

        String[] dateSplit = deadline.split("-");

        int year = Integer.parseInt(dateSplit[0]);
        int month = Integer.parseInt(dateSplit[1]);
        int day = Integer.parseInt(dateSplit[2]);

        month--;

        this.deadline = new GregorianCalendar(year, month, day);
    }

    public String getHolder() {
        return holder;
    }

    public void setHolder(String holder) {
        this.holder = holder;
    }
}
