package org.malovitsinm.pojo;

import org.malovitsinm.annotations.GeneratedBuilder;

@GeneratedBuilder
public class Sword {

    private Integer durability;

    private String title;

    private Integer damage;

    public void setDurability(Integer durability) {
        this.durability = durability;
    }

    public void setDamage(Integer damage) {
        this.damage = damage;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTitle() {
        return title;
    }

    public Integer getDurability() {
        return durability;
    }

    public Integer getDamage() {
        return damage;
    }

}
