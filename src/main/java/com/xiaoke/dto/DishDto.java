package com.xiaoke.dto;

import com.xiaoke.entity.Dish;
import com.xiaoke.entity.DishFlavor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import java.util.ArrayList;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DishDto extends Dish {
    private List<DishFlavor> flavors=new ArrayList<>();
    private String categoryName;
    private Integer copies;
}
