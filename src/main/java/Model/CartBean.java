package Model;

import java.util.ArrayList;
import java.util.List;

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

    public void setCartList(List<ProductCartBean> cartList) {
        this.cartList = new ArrayList<>(cartList);
        for (ProductCartBean productCartBean : cartList) {
            this.numberObject += productCartBean.getQuantity();
        }
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

    // FIX: rimuove solo se l'id esiste, altrimenti non fa nulla
    public void removeProduct(int id) {
        for (int i = 0; i < cartList.size(); i++) {
            if (cartList.get(i).getId() == id) {
                numberObject -= cartList.get(i).getQuantity();
                cartList.remove(i);
                return; // esci dopo aver rimosso l'elemento corretto
            }
        }
        // se non trovato, non fare nulla (il carrello rimane intatto)
    }
}