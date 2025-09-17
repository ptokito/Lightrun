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

## Demo Flow for TAM Presentations

### Phase 1: Problem Demonstration (5 minutes)
1. Show normal e-commerce functionality via web interface
2. Trigger race condition using "Trigger Race Condition" button
3. Display negative inventory levels to audience
4. Explain this represents a critical production issue

### Phase 2: Traditional Debugging Challenges (2 minutes)
- Explain typical approach: add logging, redeploy, reproduce, analyze
- Highlight time cost: hours to days for resolution
- Point out production safety concerns with code changes

### Phase 3: Lightrun Investigation (15 minutes)
1. **Setup**: Show Lightrun IDE plugin interface
2. **Dynamic Logging**: Add logs to `OrderService.createOrder()` without code changes
3. **Snapshots**: Capture variable states during race condition
4. **Root Cause**: Identify exact timing and thread collision
5. **Verification**: Confirm hypothesis with additional logs

### Phase 4: Value Proposition (3 minutes)
- Compare time to resolution: minutes vs hours
- Emphasize production safety: no deployments required
- Highlight collaboration: shareable insights across teams

## Lightrun Integration

### Agent Installation

```bash
# Download agent from Lightrun dashboard
# Run application with agent
java -javaagent:/path/to/lightrun-agent.jar \
     -Dcom.lightrun.server=https://app.lightrun.com \
     -Dcom.lightrun.secret=YOUR_SECRET_KEY \
     -jar target/demo-ecommerce-1.0.0.jar
```

### IDE Plugin Setup
1. Install Lightrun plugin from marketplace
2. Configure connection to management server
3. Authenticate with provided credentials

## Testing the Bugs

### Race Condition Test
```bash
# Manual trigger via API
for i in {1..5}; do
  curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{"customerId":"user'$i'","items":[{"productId":1,"quantity":3}]}' &
done

# Check inventory after concurrent requests
curl http://localhost:8080/api/products
```

### Pricing Bug Test
```bash
# Test discount code issue
curl -X POST http://localhost:8080/api/orders \
-H "Content-Type: application/json" \
-d '{"customerId":"test","items":[{"productId":1,"quantity":1}],"discountCode":"SAVE10"}'
```

## File Structure

```
src/
├── main/
│   ├── java/com/lightrun/demo/
│   │   └── EcommerceApplication.java    # Complete application
│   └── resources/
│       ├── application.properties        # Configuration
│       └── static/
│           └── index.html               # Web interface
└── test/
    └── java/                            # Test classes
```

## API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/products` | List all products with inventory |
| POST | `/api/orders` | Create new order |
| GET | `/api/orders` | List all orders |
| GET | `/api/orders/{id}` | Get specific order |

## Value Proposition Highlights

### Speed
- **Traditional**: Hours to days for root cause analysis
- **Lightrun**: Minutes to identify exact issue location

### Safety  
- **Traditional**: Requires code changes and deployment cycles
- **Lightrun**: Zero code changes, production-safe debugging

### Precision
- **Traditional**: Guesswork based on limited static logs
- **Lightrun**: Exact variable states and execution flow

### Collaboration
- **Traditional**: Knowledge siloed in individual developers
- **Lightrun**: Shareable debugging actions and insights

## Technical Specifications

### Dependencies
- Spring Boot Starter Web
- Spring Boot Starter Actuator
- Java 17 Runtime

### Performance Characteristics
- Memory footprint: ~100MB
- Startup time: ~3 seconds
- Concurrent user support: 100+

### Browser Compatibility
- Chrome 90+
- Firefox 88+
- Safari 14+
- Edge 90+

## Interview Preparation

### Key Talking Points
1. **Problem Statement**: Production bugs are costly and time-consuming
2. **Traditional Limitations**: Static logging, deployment cycles, reproduction challenges
3. **Lightrun Solution**: Dynamic instrumentation, real-time insights
4. **Business Impact**: Faster MTTR, higher quality, improved productivity

### Common Questions
- **"How does this compare to APM tools?"** - Complement each other; APM alerts, Lightrun investigates
- **"Is this safe for production?"** - Built-in safeguards, quotas, non-blocking execution
- **"What about performance impact?"** - Minimal overhead, asynchronous processing

## Contributing

This is a demonstration application for educational purposes. The bugs are intentional and should not be "fixed" as they serve the demo narrative.

## License

Educational use only - Created for Lightrun Technical Account Manager interview demonstration.

---

**Demo Application**: Showcases real-time debugging capabilities  
**Target Audience**: Development teams, DevOps engineers, Technical decision makers  
**Created By**: Technical Account Manager Candidate  
**Purpose**: Customer enablement and technical demonstration
