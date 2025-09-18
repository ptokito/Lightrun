# Lightrun Technical Account Manager Demo

A Java Spring Boot e-commerce application designed to demonstrate Lightrun's real-time debugging capabilities during technical presentations and customer demonstrations.

## Overview

This application contains strategically placed bugs that showcase how Lightrun revolutionizes production debugging by enabling real-time logs, metrics, and snapshots without code changes, deployments, or application restarts.

## Architecture

- **Framework**: Spring Boot 2.7.18
- **Java Version**: 17
- **Build Tool**: Maven
- **Storage**: In-memory (ConcurrentHashMap)
- **UI**: Responsive web interface with Lightrun branding

## Key Features

### Intentional Bugs for Demonstration

1. **Race Condition in Inventory Management**
   - **Location**: `OrderService.createOrder()` method
   - **Issue**: Non-atomic inventory check and update operations
   - **Impact**: Allows negative inventory levels under concurrent load
   - **Demo Value**: Perfect for showing thread collision detection

2. **Float Precision Errors in Financial Calculations**
   - **Location**: `PricingService.applyDiscount()` method  
   - **Issue**: Using float arithmetic instead of BigDecimal for money
   - **Impact**: Pricing discrepancies in discount calculations
   - **Demo Value**: Demonstrates precision loss investigation

3. **Missing Business Logic Implementation**
   - **Location**: `EcommerceController.createOrder()` method
   - **Issue**: Discount codes set but totals not recalculated
   - **Impact**: Customers charged full price despite valid discount codes
   - **Demo Value**: Shows execution flow tracing

## Quick Start

### Prerequisites

```bash
# Java 17
java -version

# Maven 3.6+
mvn -version
```

### Running the Application

```bash
# Clone repository
git clone https://github.com/ptokito/Lightrun.git
cd Lightrun

# Build and run
mvn clean package
mvn spring-boot:run
```

### Access Points

- **Web Interface**: http://localhost:8080
- **Products API**: http://localhost:8080/api/products
- **Orders API**: http://localhost:8080/api/orders
