package Model;

import java.util.ArrayList;

public class Cart {

    private int numberObject;
    private ArrayList<ProductCartBean> cartList;

    public Cart() {
        cartList = new ArrayList<>();
        numberObject = 0;
    }

    public int getNumberObject() {
        return numberObject;
    }

    public void setNumberObject(int numberObject) {
        this.numberObject = numberObject;
    }

    public ArrayList<ProductCartBean> getCartList() {
        return cartList;
    }

    public void setCartList(ArrayList<ProductCartBean> cartList) {
        this.cartList = cartList;
    }

    public void addProduct(int id, int quantity) {

        numberObject++;
        boolean isOn = false;
        for (ProductCartBean product : cartList) {

            if (product.getId() == id) {

                isOn = true;
                int newQuantity = (product.getQuantity() + 1);
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

                int newQuantity = (product.getQuantity() - 1);

                if (newQuantity == 0)
                    cartList.remove(product);
                else
                    product.setQuantity(newQuantity);
            }
        }
    }

    public ArrayList<ProductCartBean> getList() {

        return cartList;
    }
}