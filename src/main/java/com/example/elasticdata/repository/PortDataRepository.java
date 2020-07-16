package com.example.elasticdata.repository;

import com.example.elasticdata.entity.other.Port;
import org.springframework.data.repository.CrudRepository;

public interface PortDataRepository extends CrudRepository<Port,String> {
}
