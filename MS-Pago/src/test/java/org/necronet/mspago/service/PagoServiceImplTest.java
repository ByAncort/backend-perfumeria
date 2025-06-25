package org.necronet.mspago.service;


import net.datafaker.Faker;
import org.app.dto.ServiceResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.necronet.mspago.client.MicroserviceClient;
import org.necronet.mspago.client.TokenContext;
import org.necronet.mspago.dto.CarritoResponse;
import org.necronet.mspago.model.*;
import org.necronet.mspago.repository.PagoRepository;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceImplTest {

}
