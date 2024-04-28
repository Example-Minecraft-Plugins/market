package me.davipccunha.tests.market.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Getter
public class PersonalMarket {
    private final String owner;
    private final List<MarketProduct> products = new ArrayList<>();

    public void addProduct(MarketProduct product) {
        this.products.add(product);
    }

    public void removeProduct(MarketProduct product) {
        this.products.remove(product);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PersonalMarket that = (PersonalMarket) obj;
        return owner.equals(that.owner);
    }
}
