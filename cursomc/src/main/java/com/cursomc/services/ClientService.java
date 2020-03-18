package com.cursomc.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cursomc.domain.Address;
import com.cursomc.domain.City;
import com.cursomc.domain.Client;
import com.cursomc.domain.enums.ClientType;
import com.cursomc.dto.ClientDTO;
import com.cursomc.dto.NewClientDTO;
import com.cursomc.repositories.AddressRepository;
import com.cursomc.repositories.ClientRepository;
import com.cursomc.services.exceptions.DataIntegrityException;
import com.cursomc.services.exceptions.ObjectNotFoundException;

@Service
public class ClientService {
	
	@Autowired
	ClientRepository rep;
	
	@Autowired
	AddressRepository addressRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	public Client findById(Integer id) {
		Optional<Client> client = rep.findById(id);
		return client.orElseThrow(() -> new ObjectNotFoundException("Objeto não encontrado! Id: " + id + ", Tipo: " + Client.class.getName()));
	}
	
	@Transactional
	public Client insert(Client client) {
		client.setId(null);
		client = rep.save(client);
		addressRepository.saveAll(client.getAddresses());
		return client;
	}
	
	public Client update(Client client) {
		Client newClient = findById(client.getId());
		updateData(newClient, client);
		return rep.save(newClient);
	}
	
	public void deleteById(Integer id) {
		findById(id);
		try {
			rep.deleteById(id);
		} catch (DataIntegrityViolationException e) {
			throw new DataIntegrityException("Não é possível excluir o cliente pois existe(m) pedido(s) vinculado(s).");
		}
	}
	
	public List<Client> findAll() {
		return rep.findAll();
	}
	
	public Page<Client> findPage(Integer page, Integer linesPerPage, String direction, String orderBy){
		PageRequest pageRequest = PageRequest.of(page, linesPerPage, Direction.valueOf(direction), orderBy);
		return rep.findAll(pageRequest);
 	}
	
	public Client fromDto(ClientDTO clientDto) {
		return new Client(clientDto.getId(), clientDto.getName(), clientDto.getEmail(), null, null, null);
	}
	
	public Client fromDto(NewClientDTO newClientDto) {
		Client client = new Client(null, newClientDto.getName(), newClientDto.getEmail(), bCryptPasswordEncoder.encode(newClientDto.getPassword()), newClientDto.getCpfOrCnpj(), ClientType.toEnum(newClientDto.getType()));
		City city = new City(newClientDto.getCityId(), null, null);
		Address address = new Address(null, newClientDto.getStreet(), newClientDto.getNumber(), newClientDto.getComplement(), newClientDto.getNeighborhood(), newClientDto.getCep(), client, city);
		client.getAddresses().add(address);
		client.getPhones().add(newClientDto.getPhone1());
		if(newClientDto.getPhone2() != null) {
			client.getPhones().add(newClientDto.getPhone2());
		}
		if(newClientDto.getPhone3() != null) {
			client.getPhones().add(newClientDto.getPhone3());
		}
		return client;
	}
	
	private void updateData(Client newClient, Client client) {
		newClient.setName(client.getName());
		newClient.setEmail(client.getEmail());
	}

}