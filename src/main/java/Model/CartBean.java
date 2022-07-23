package Model;

import java.util.ArrayList;

public class CartBean {

    private int numberObject;
    private ArrayList<ProductCartBean> cartList;

    public CartBean() {
        cartList = new ArrayList<>();
        numberObject = 0;
    }

    public int getNumberObject() {

        return numberObject;
    }

    public void setNumberObject(int number) {

        this.numberObject = number;
    }

    public ArrayList<ProductCartBean> getCartList() {

        return cartList;
    }

    public void setCartList(ArrayList<ProductCartBean> cartList) {
        this.cartList = cartList;

        for (ProductCartBean productCartBean: cartList)
            this.numberObject += productCartBean.getQuantity();
    }

    public void addProduct(int id, int quantity) {

        numberObject += quantity;
        boolean isOn = false;
        for (ProductCartBean product : cartList) {

            if (product.getId() == id) {

                isOn = true;
                int newQuantity = (product.getQuantity() + quantity);
                product.setQuantity(newQuantity);
            }
        }

        if (!isOn) {

            ProductCartBean product = new ProductCartBean();
            product.setId(id);
            product.setQuantity(quantity);
            cartList.add(product);
        }
    }

    public void removeProduct(int id) {

        for (ProductCartBean product : cartList) {

            if (product.getId() == id) {
                this.numberObject -= product.getQuantity();
                cartList.remove(product);
            }
        }
    }
}