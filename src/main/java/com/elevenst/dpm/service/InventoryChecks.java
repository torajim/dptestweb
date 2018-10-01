package com.elevenst.dpm.service;

import java.util.*;

public class InventoryChecks {
    public enum INVENTORY{
        BILLBOARD("상단 빌보드"),
        BELT_BANNER("띠배너"),
        EXHIBITION_1("기획전1"),
        EMERGENCY_PROCURE("긴급공수"),
        SHOCKING_DEAL("쇼킹딜 상품"),
        SHOCKING_DEAL_BELT_BANNER("쇼킹딜 띠배너"),
        MID_BILLBOARD("중단 빌보드"),
        MOVIE_CLIP("동영상"),
        HIT("히트상품"),
        MD("MD추천");

        private final String description;

        INVENTORY(String description){
            this.description = description;
        }

        public static INVENTORY fromString(String description){
            for(INVENTORY inventory : INVENTORY.values()){
                if(inventory.description.equalsIgnoreCase(description)){
                    return inventory;
                }
            }
            return null;
        }

        public String getDescription(){
            return this.description;
        }
    }

    private Set<INVENTORY> referenceFullSet;
    private Map<INVENTORY, Boolean> resultMap;
    private Set<INVENTORY> inventorySet;

    public InventoryChecks(INVENTORY... intentories){
        inventorySet = new HashSet<>();
        inventorySet.addAll(Arrays.asList(intentories));
        makeFullSet();
    }

    public InventoryChecks(Set<String> inventories){
        inventorySet = new HashSet<>();
        for(String item : inventories){
            inventorySet.add(INVENTORY.fromString(item));
        }
        makeFullSet();
    }

    private void makeFullSet(){
        referenceFullSet = new HashSet<>();
        referenceFullSet.add(INVENTORY.BILLBOARD);
        referenceFullSet.add(INVENTORY.BELT_BANNER);
        referenceFullSet.add(INVENTORY.EXHIBITION_1);
        referenceFullSet.add(INVENTORY.EMERGENCY_PROCURE);
        referenceFullSet.add(INVENTORY.SHOCKING_DEAL);
        referenceFullSet.add(INVENTORY.SHOCKING_DEAL_BELT_BANNER);
        referenceFullSet.add(INVENTORY.MID_BILLBOARD);
        referenceFullSet.add(INVENTORY.MOVIE_CLIP);
        referenceFullSet.add(INVENTORY.HIT);
        referenceFullSet.add(INVENTORY.MD);

        resultMap = new HashMap<>();
        referenceFullSet.forEach(v -> resultMap.put(v, Boolean.FALSE));
        inventorySet.forEach(v -> resultMap.put(v, Boolean.TRUE));
    }

    public boolean isSet(INVENTORY inventory){
        return inventorySet.contains(inventory);
    }

    public Set<INVENTORY> setInventory(INVENTORY inventory){
        inventorySet.add(inventory);
        resultMap.put(inventory, Boolean.TRUE);
        return this.inventorySet;
    }

    public Set<INVENTORY> unsetInventory(INVENTORY inventory){
        inventorySet.remove(inventory);
        resultMap.put(inventory, Boolean.FALSE);
        return this.inventorySet;
    }

    public Map<INVENTORY, Boolean> checkStatus(){
        return this.resultMap;
    }
}