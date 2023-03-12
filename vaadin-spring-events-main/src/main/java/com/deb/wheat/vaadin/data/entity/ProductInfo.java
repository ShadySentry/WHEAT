package com.deb.wheat.vaadin.data.entity;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Builder
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
@Entity
public class ProductInfo extends AbstractEntity{

    @Column(unique = true)
    private String productIdentityInfo;

    private String description;

    @OneToMany(mappedBy = "productInfo")
    List<OperatorToDoItem> todos;

    public void addToDoItem(OperatorToDoItem newItem){
        if (todos==null){
            todos=new ArrayList<>();
        }

        if(!toDoItemExists(newItem)) {//todo replace with toDoItemExists()
            todos.add(newItem);
        }
    }

    public void removeToDoItem(OperatorToDoItem item){
        if(todos==null){
            return;
        }
        todos.remove(item);
    }

    public boolean toDoItemExists(OperatorToDoItem item){
        boolean exists = false;
        for(OperatorToDoItem toDoItem:todos){
            if (toDoItem.getOperation().getOperationNumber()==item.getOperation().getOperationNumber()
            && toDoItem.getActionDescriptions().getActionNumber()==item.getActionDescriptions().getActionNumber()){
                exists=true;
                return exists;
            }
        }
        return exists;
    }
}
