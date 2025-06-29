Basándome en el código del repositorio `ByAncort/backend-perfumeria`, aquí tienes el System Overview completo actualizado con todos los componentes y diagramas de arquitectura:

# System Overview

Este documento proporciona una visión general del sistema Perfumeria Backend, una arquitectura basada en microservicios diseñada para soportar un negocio de venta de perfumes. El sistema está compuesto por múltiples microservicios especializados que trabajan juntos para proporcionar funcionalidad integral para gestión de inventario, procesamiento de ventas, administración de usuarios y más. [1](#3-0) 

## Arquitectura General del Sistema

```mermaid
graph TB
    subgraph "Frontend Layer"
        WEB[Web Application]
        MOBILE[Mobile App]
        API_GW[API Gateway]
    end
    
    subgraph "Authentication & Security"
        AUTH[MS-Authenticacion:9010]
        USERS[MS-Usuarios:9011]
    end
    
    subgraph "Core Business Services"
        PROD[MS-Productos:9015]
        PROV[MS-Proveedores:9014]
        SUC[MS-Sucursales:9016]
        INV[MS-Inventario:9017]
    end
    
    subgraph "E-commerce Services"
        CLIENT[MS-Cliente:9012]
        CART[MS-Carrito:9018]
        COUPON[MS-cupones:9022]
        SALES[MS-Ventas:9013]
    end
    
    subgraph "Operations Services"
        PAY[MS-Pago:9019]
        SUPPORT[MS-SoporteCliente:9020]
        REVIEW[MS-ResenasFeedBack:9021]
    end
    
    subgraph "Data Layer"
        DB[(MySQL Database<br/>perfumalandia_spa)]
    end
    
    WEB --> API_GW
    MOBILE --> API_GW
    API_GW --> AUTH
    
    AUTH -.->|JWT Validation| USERS
    AUTH -.->|JWT Validation| PROD
    AUTH -.->|JWT Validation| PROV
    AUTH -.->|JWT Validation| SUC
    AUTH -.->|JWT Validation| INV
    AUTH -.->|JWT Validation| CLIENT
    AUTH -.->|JWT Validation| CART
    AUTH -.->|JWT Validation| COUPON
    AUTH -.->|JWT Validation| SALES
    AUTH -.->|JWT Validation| PAY
    AUTH -.->|JWT Validation| SUPPORT
    AUTH -.->|JWT Validation| REVIEW
    
    CART --> INV
    CART --> COUPON
    CART --> PAY
    SALES --> INV
    SALES --> PROD
    SUPPORT --> CLIENT
    REVIEW --> PROD
    REVIEW --> CLIENT
    
    AUTH --> DB
    USERS --> DB
    PROD --> DB
    PROV --> DB
    SUC --> DB
    INV --> DB
    CLIENT --> DB
    CART --> DB
    COUPON --> DB
    SALES --> DB
    PAY --> DB
    SUPPORT --> DB
    REVIEW --> DB
```

## Stack Tecnológico

```mermaid
graph TB
    subgraph "Backend Framework"
        SPRING[Spring Boot 3.5.3]
        JAVA[Java 21]
        MAVEN[Maven Build Tool]
    end
    
    subgraph "Security & Authentication"
        JWT[JWT Tokens v0.12.6]
        SPRING_SEC[Spring Security]
    end
    
    subgraph "Communication & Integration"
        REST[REST APIs]
        HATEOAS[Spring HATEOAS v2.5.1]
        OPENAPI[OpenAPI/Swagger v2.8.9]
        HTTP[HTTP Client - RestTemplate]
    end
    
    subgraph "Resilience & Performance"
        CIRCUIT[Resilience4j v2.3.0]
        CACHE[Spring Cache v3.5.0]
    end
    
    subgraph "Data Layer"
        MYSQL[MySQL Database]
        JPA[Spring Data JPA]
        HIBERNATE[Hibernate ORM]
    end
    
    SPRING --> JAVA
    SPRING --> JWT
    SPRING --> REST
    SPRING --> JPA
    REST --> HATEOAS
    REST --> OPENAPI
    CIRCUIT --> CACHE
    JPA --> MYSQL
```

## Arquitectura de Microservicios

### Microservicios Actualizados

| Microservicio | Puerto | Responsabilidad Principal | Características Clave |
|---------------|--------|---------------------------|----------------------|
| MS-Authenticacion | 9010 | Autenticación, generación y validación de tokens | Tokens JWT, login/registro |
| MS-Usuarios | 9011 | Gestión de usuarios, roles y permisos | Sistema RBAC con roles/permisos |
| MS-Cliente | 9012 | Gestión de datos y perfiles de clientes | CRUD de clientes, validación de datos |
| MS-Ventas | 9013 | Procesamiento y gestión de ventas | Procesamiento de transacciones, cancelación de ventas |
| MS-Proveedores | 9014 | Gestión de proveedores/suministradores | **Validación de RUT chileno**, CRUD de proveedores |
| MS-Productos | 9015 | Gestión de catálogo de productos | Productos, categorías, gestión de SKU, Circuit Breaker |
| MS-Sucursales | 9016 | Gestión de sucursales/tiendas | Ubicaciones de tiendas, horarios de operación |
| MS-Inventario | 9017 | Seguimiento de inventario entre sucursales | Gestión de stock, transferencias, alertas de stock bajo |
| **MS-Carrito** | **9018** | **Gestión de carrito de compras** | **Estados de carrito, cupones, HATEOAS** |
| **MS-Pago** | **9019** | **Procesamiento de pagos y reembolsos** | **Múltiples métodos de pago, HATEOAS** |
| **MS-SoporteCliente** | **9020** | **Sistema de tickets de soporte** | **Gestión de tickets, integración con clientes** |
| **MS-ResenasFeedBack** | **9021** | **Sistema de reseñas y feedback** | **Validación dual de productos y clientes** |
| **MS-cupones** | **9022** | **Gestión de cupones y descuentos** | **Cupones porcentuales y fijos** | [2](#3-1) 

## Patrón de Comunicación Entre Servicios

```mermaid
sequenceDiagram
    participant Client as "Cliente/Frontend"
    participant AuthMS as "MS-Authenticacion"
    participant ServiceA as "Servicio Origen<br/>(ej: MS-Carrito)"
    participant ServiceB as "Servicio Destino<br/>(ej: MS-Inventario)"
    participant TokenCtx as "TokenContext"
    participant MicroClient as "MicroserviceClient"
    
    Note over Client,MicroClient: Flujo de Autenticación
    Client->>AuthMS: "POST /api/auth/login"
    AuthMS-->>Client: "JWT Token"
    
    Note over Client,MicroClient: Llamada a Servicio con Comunicación Inter-Servicio
    Client->>ServiceA: "API Request + Bearer Token"
    ServiceA->>AuthMS: "Validar Token"
    AuthMS-->>ServiceA: "Token Válido"
    
    ServiceA->>TokenCtx: "setToken(jwt)"
    ServiceA->>ServiceA: "Lógica de Negocio"
    
    alt "Necesita datos de otro servicio"
        ServiceA->>TokenCtx: "getToken()"
        TokenCtx-->>ServiceA: "JWT Token"
        ServiceA->>MicroClient: "enviarConToken(url, method, body, class, token)"
        MicroClient->>ServiceB: "HTTP Request + Authorization: Bearer {token}"
        ServiceB->>AuthMS: "Validar Token"
        AuthMS-->>ServiceB: "Token Válido"
        ServiceB->>ServiceB: "Procesar Request"
        ServiceB-->>MicroClient: "ServiceResult<T>"
        MicroClient-->>ServiceA: "ResponseEntity<T>"
    end
    
    ServiceA-->>Client: "ServiceResult con datos combinados"
```

Los componentes clave para la comunicación entre servicios son:
- `TokenContext`: Almacena y proporciona el token JWT actual
- `MicroserviceClient`: Un wrapper alrededor de RestTemplate que agrega el token a las solicitudes
- `ServiceResult<T>`: Un wrapper de respuesta estandarizado que incluye datos o información de error [3](#3-2) 

## Nuevos Servicios de E-commerce

### MS-Carrito (Puerto 9018)
Servicio de carrito de compras con integración completa de cupones y validación de inventario.

**Características principales:**
- Estados de carrito: ACTIVO, VACIO, COMPLETADO, ABANDONADO [4](#3-3) 
- Integración con MS-Inventario para validación de stock [5](#3-4) 
- Soporte completo para cupones de descuento [6](#3-5) 
- APIs HATEOAS para navegación de recursos [7](#3-6) 

### MS-Pago (Puerto 9019)
Servicio de procesamiento de pagos con soporte para múltiples métodos de pago y reembolsos.

**Características principales:**
- Métodos de pago: TARJETA_CREDITO, PAYPAL, TRANSFERENCIA [8](#3-7) 
- Sistema de reembolsos [9](#3-8) 
- Validación de carritos antes del pago
- APIs HATEOAS completas con enlaces condicionales

### MS-SoporteCliente (Puerto 9020)
Sistema de tickets de soporte al cliente con integración a datos de clientes. [10](#3-9) 

**Características principales:**
- Gestión completa de tickets de soporte
- Validación de clientes con MS-Cliente [11](#3-10) 
- Estados de tickets y seguimiento
- Integración JWT para seguridad

## Patrones de Diseño Implementados

### 1. Circuit Breaker Pattern [12](#3-11) 

```mermaid
stateDiagram-v2
    [*] --> Closed: "Servicio funcionando"
    Closed --> Open: "Fallas >= threshold"
    Open --> HalfOpen: "Timeout alcanzado"
    HalfOpen --> Closed: "Request exitoso"
    HalfOpen --> Open: "Request falla"
    
    Closed: Requests pasan normalmente
    Open: Requests fallan rápidamente
    HalfOpen: Permite requests de prueba
```

### 2. HATEOAS Pattern

```mermaid
graph LR
    Resource[EntityModel Resource] --> SelfLink[Self Link]
    Resource --> RelatedLinks[Related Links]
    Resource --> ConditionalLinks[Conditional Links]
    
    SelfLink --> |"withSelfRel()"| SelfRef[Self Reference]
    RelatedLinks --> |"withRel('view-cart')"| ViewCart[View Cart]
    RelatedLinks --> |"withRel('apply-coupon')"| ApplyCoupon[Apply Coupon]
    ConditionalLinks --> |"if(estado == 'COMPLETADO')"| Reembolso[Reembolso Link]
```

## Flujo de Negocio: Proceso de Compra Completo

```mermaid
sequenceDiagram
    participant Cliente
    participant MSCarrito as "MS-Carrito"
    participant MSInventario as "MS-Inventario"
    participant MSCupones as "MS-cupones"
    participant MSPago as "MS-Pago"
    
    Cliente->>MSCarrito: "Agregar productos al carrito"
    MSCarrito->>MSInventario: "Validar stock disponible"
    MSInventario-->>MSCarrito: "Stock confirmado"
    
    opt "Aplicar cupón"
        Cliente->>MSCarrito: "Aplicar código de cupón"
        MSCarrito->>MSCupones: "Validar cupón"
        MSCupones-->>MSCarrito: "Cupón válido + descuento"
        MSCarrito->>MSCarrito: "Calcular precio final"
    end
    
    Cliente->>MSCarrito: "Confirmar carrito"
    MSCarrito->>MSCarrito: "Estado: ACTIVO → COMPLETADO"
    
    Cliente->>MSPago: "Procesar pago"
    MSPago->>MSPago: "Validar método de pago"
    MSPago->>MSInventario: "Reducir stock"
    MSInventario-->>MSPago: "Stock actualizado"
    MSPago-->>Cliente: "Pago confirmado"
```

## Autenticación y Seguridad

El sistema utiliza un mecanismo de autenticación basado en JWT. MS-Auth genera tokens al iniciar sesión, que luego son validados por otros microservicios al procesar solicitudes. [13](#3-12) 

### TokenContext Pattern [14](#3-13) 

## Contexto de Negocio Chileno y Validación de RUT

El sistema está específicamente diseñado para el mercado chileno, con MS-Proveedores

