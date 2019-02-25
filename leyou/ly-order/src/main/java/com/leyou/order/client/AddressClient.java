package com.leyou.order.client;

import com.leyou.order.dto.AddressDTO;

import java.util.ArrayList;
import java.util.List;

public abstract class AddressClient {

    public static final List<AddressDTO> addressList = new ArrayList<>();

        static {
            AddressDTO address01 = new AddressDTO();
            address01.setId(1L);
            address01.setAddress("北京市昌平区4号");
            address01.setCity("北京");
            address01.setDistrict("昌平区");
            address01.setPhone("1336667824");
            address01.setState("北京");
            address01.setZipCode("100002");
            address01.setIsDefault(false);

            addressList.add(address01);

            AddressDTO address02 = new AddressDTO();
            address02.setId(1L);
            address02.setAddress("北京市昌平区4号");
            address02.setCity("北京");
            address02.setDistrict("昌平区");
            address02.setPhone("1336667824");
            address02.setState("北京");
            address02.setZipCode("100002");
            address02.setIsDefault(false);

            addressList.add(address02);

        }


    public static AddressDTO findById(Long id) {
        for (AddressDTO addressDTO : addressList) {
            if (addressDTO.getId() == id) {
                return addressDTO;
            }
        }

        return null;
    }
}