# QA Test Audit

## Overview

This document summarizes completed test coverage work across backend and frontend, execution status, known risks, and recommended next steps.

## What Was Covered

### Backend Auth / Access

- **Covered files**
  - `src/main/java/com/team7/security/SecurityConfig.java`
  - `src/main/java/com/team7/api/controller/AuthController.java`
  - role-protected controllers under `src/main/java/com/team7/api/controller/**`
- **Created tests**
  - `src/test/java/com/team7/api/AuthAccessIntegrationTest.java`
  - `src/test/java/com/team7/api/RoleAccessMatrixIntegrationTest.java`
  - `src/test/java/com/team7/service/client/AuthServiceImplTest.java`
  - (extended existing) `src/test/java/com/team7/api/RestApiIntegrationTest.java`
- **Key scenarios**
  - auth `/me` success/failure paths
  - unauthorized and wrong-role access checks
  - missing linked profile errors
  - auth validation and unified error shape checks
  - unit coverage for `AuthServiceImpl` flows (register/login/logout/profile/password)
- **Execution**
  - Static verification only (no backend runtime test execution in current environment)

### Backend Order / Cart

- **Covered files**
  - `src/main/java/com/team7/api/controller/OrderController.java`
  - `src/main/java/com/team7/api/controller/CartController.java`
  - `src/main/java/com/team7/service/client/OrderServiceImpl.java`
  - `src/main/java/com/team7/service/client/CartServiceImpl.java`
- **Created tests**
  - `src/test/java/com/team7/api/OrderControllerIntegrationTest.java`
  - `src/test/java/com/team7/api/CartControllerIntegrationTest.java`
  - `src/test/java/com/team7/service/client/OrderServiceImplTest.java`
  - `src/test/java/com/team7/service/client/CartServiceImplTest.java`
- **Key scenarios**
  - create/get/list/cancel/repeat order API behavior
  - cart CRUD actions and validation errors
  - empty/error/happy-path behavior
  - service orchestration checks (cart clear, status updates, error propagation)
- **Execution**
  - Static verification only

### Backend Menu / Reviews

- **Covered files**
  - `src/main/java/com/team7/api/controller/restaurant/RestaurantManagementController.java`
  - `src/main/java/com/team7/api/controller/client/CourierReviewController.java`
  - `src/main/java/com/team7/api/controller/admin/AdminCourierReviewController.java`
  - `src/main/java/com/team7/service/restaurant/RestaurantManagementService.java`
  - `src/main/java/com/team7/service/client/CourierReviewService.java`
  - `src/main/java/com/team7/service/client/ReviewServiceImpl.java`
- **Created tests**
  - `src/test/java/com/team7/api/RestaurantManagementControllerIntegrationTest.java`
  - `src/test/java/com/team7/api/ReviewControllerIntegrationTest.java`
  - `src/test/java/com/team7/service/restaurant/RestaurantManagementServiceTest.java`
  - `src/test/java/com/team7/service/client/CourierReviewServiceTest.java`
  - `src/test/java/com/team7/service/client/ReviewServiceImplTest.java`
- **Key scenarios**
  - menu CRUD and ownership restrictions
  - review create/list/delete with role restrictions
  - validation and domain errors
  - service-level business rules and mapping behavior
- **Execution**
  - Static verification only

### Backend Role-Specific Order Flows

- **Covered files**
  - `src/main/java/com/team7/api/controller/courier/CourierController.java`
  - `src/main/java/com/team7/api/controller/restaurant/RestaurantManagementController.java` (order/status part)
  - `src/main/java/com/team7/api/controller/admin/AdminController.java`
  - `src/main/java/com/team7/service/courier/CourierService.java`
  - `src/main/java/com/team7/service/admin/AdminService.java`
  - `src/main/java/com/team7/service/restaurant/RestaurantManagementService.java` (order/status behavior)
- **Created tests**
  - `src/test/java/com/team7/api/CourierControllerIntegrationTest.java`
  - `src/test/java/com/team7/api/RestaurantOrderFlowIntegrationTest.java`
  - `src/test/java/com/team7/api/AdminControllerIntegrationTest.java`
  - `src/test/java/com/team7/service/courier/CourierServiceTest.java`
  - `src/test/java/com/team7/service/admin/AdminServiceTest.java`
  - `src/test/java/com/team7/service/restaurant/RestaurantOrderStatusServiceTest.java`
- **Key scenarios**
  - role guards (401/403), linked-profile missing, ownership/assignment restrictions
  - empty/non-empty lists
  - claim/update actions and domain errors
  - status behavior as currently implemented
- **Execution**
  - Static verification only

### Backend Repository / Transaction Tests

- **Covered files**
  - `src/main/java/com/team7/persistence/CourierAssignedOrderJpaRepository.java`
  - `src/main/java/com/team7/persistence/OrderJpaRepository.java`
  - `src/main/java/com/team7/persistence/OrderStatusHistoryJpaRepository.java`
  - transactional flows via:
    - `src/main/java/com/team7/repository/client/OrderRepository.java`
    - `src/main/java/com/team7/service/courier/CourierService.java`
- **Created tests**
  - `src/test/java/com/team7/persistence/CourierAssignedOrderJpaRepositoryTest.java`
  - `src/test/java/com/team7/persistence/OrderJpaRepositoryTest.java`
  - `src/test/java/com/team7/persistence/OrderStatusHistoryJpaRepositoryTest.java`
  - `src/test/java/com/team7/repository/client/OrderRepositoryTransactionTest.java`
  - `src/test/java/com/team7/service/courier/CourierServiceTransactionTest.java`
- **Key scenarios**
  - assignment lookups and anti-leak checks
  - role-specific order selections and sorting
  - availability query behavior with delivery/status/assignment filters
  - status history persistence and order separation
  - transactional side effects for create/cancel/claim/status-update
- **Execution**
  - Static verification only

### Frontend Auth / Route Guards

- **Covered files**
  - `frontend/src/context/AuthContext.jsx`
  - `frontend/src/components/ProtectedRoute.jsx`
  - `frontend/src/App.jsx`
  - `frontend/src/services/apiClient.js`
- **Created tests**
  - `frontend/src/context/__tests__/AuthContext.test.jsx`
  - `frontend/src/components/__tests__/ProtectedRoute.test.jsx`
  - `frontend/src/__tests__/AppRoutes.test.jsx`
  - `frontend/src/services/__tests__/apiClient.test.js`
- **Key scenarios**
  - auth init/login/logout/session-expired handling
  - role-based navigation decisions
  - route protection for public/protected/role-specific paths
  - API auth-header + 401 unauthorized handler behavior
- **Execution**
  - Executed with `npm test` (Vitest)

### Frontend Cart / Order / Restaurants / Menu Flows

- **Covered files**
  - `frontend/src/pages/CartPage.jsx`
  - `frontend/src/pages/OrderHistoryPage.jsx`
  - `frontend/src/pages/RestaurantsPage.jsx`
  - `frontend/src/pages/RestaurantMenuPage.jsx`
- **Created tests**
  - `frontend/src/pages/__tests__/CartPage.test.jsx`
  - `frontend/src/pages/__tests__/OrderHistoryPage.test.jsx`
  - `frontend/src/pages/__tests__/RestaurantsPage.test.jsx`
  - `frontend/src/pages/__tests__/RestaurantMenuPage.test.jsx`
- **Key scenarios**
  - loading/empty/error states
  - user actions (add/update/remove/checkout/cancel/details/review trigger where present)
  - success and backend domain error display
  - filter behavior for restaurant list
- **Execution**
  - Executed with `npm test`

### Frontend Dashboards

- **Covered files**
  - `frontend/src/pages/CourierDashboardPage.jsx`
  - `frontend/src/pages/RestaurantDashboardPage.jsx`
  - `frontend/src/pages/AdminDashboardPage.jsx`
- **Created tests**
  - `frontend/src/pages/__tests__/CourierDashboardPage.test.jsx`
  - `frontend/src/pages/__tests__/RestaurantDashboardPage.test.jsx`
  - `frontend/src/pages/__tests__/AdminDashboardPage.test.jsx`
- **Key scenarios**
  - loading/empty/error states
  - role-context actions and UI refresh after successful actions
  - domain/backend error visibility
  - claim/status/menu/account-toggle critical actions
- **Execution**
  - Executed with `npm test`

### Frontend Service Contract Tests

- **Covered files**
  - `frontend/src/services/authApi.js`
  - `frontend/src/services/cartApi.js`
  - `frontend/src/services/orderApi.js`
  - `frontend/src/services/restaurantsApi.js`
- **Created tests**
  - `frontend/src/services/__tests__/authApi.test.js`
  - `frontend/src/services/__tests__/cartApi.test.js`
  - `frontend/src/services/__tests__/orderApi.test.js`
  - `frontend/src/services/__tests__/restaurantsApi.test.js`
- **Key scenarios**
  - 200 success
  - 400/401/403/500 handling
  - `success:false` payload contract behavior
  - invalid JSON
  - unexpected response shape
  - error propagation to callers
- **Execution**
  - Executed with `npm test`

## Execution Readiness

- **Backend readiness:** prepared
  - Maven Wrapper added: `mvnw`, `mvnw.cmd`, `.mvn/wrapper/maven-wrapper.properties`
  - CI backend path added: `.github/workflows/tests.yml` (`./mvnw -B -Dspring.profiles.active=test test`)
  - Backend test profile remains `test` with H2 via `src/test/resources/application-test.yml`
- **Frontend readiness:** prepared
  - CI frontend path added in `.github/workflows/tests.yml` (`npm test` in `frontend`)
  - Local command documented in `README.md`
- **Execution confirmed in this agent:** partial
  - Frontend: previously confirmed (`npm test`)
  - Backend: not confirmed in this agent runtime (environment limitation), pending CI/local verification with wrapper

## What Was Actually Executed

- **Frontend**
  - Command: `npm test` in `frontend`
  - Latest result: **17 test files, 76 tests passed**
- **Backend**
  - New backend tests were added but **not runtime-executed** in this agent
  - Readiness update: Maven Wrapper + CI workflow are now configured for executable runs
  - Local check performed: `mvnw.cmd -v` succeeds (wrapper bootstraps Maven)
  - Runtime attempt result: `mvnw.cmd "-Dspring.profiles.active=test" test` fails in this agent because only JRE is available (no JDK compiler)
  - Backend verification status in this agent: runtime readiness prepared, execution still pending successful JDK-based run in CI/local dev environment

## Known Risks

1. **Owner-check boundary is now enforced at controller layer (residual architectural debt)**
   - **Severity:** low
   - **Where:** `OrderController` + service access pattern (`/api/orders/{id}` and related)
   - **Risk:** user ownership is protected now, but policy is controller-boundary logic (not centralized in dedicated security policy component)
   - **Recommended action:** keep current protection, and later centralize ownership policy in reusable security layer if broader role matrix evolves

2. **Restaurant status transitions are now validated (residual policy debt)**
   - **Severity:** low
   - **Where:** `RestaurantManagementService.updateRestaurantOrderStatus`
   - **Risk:** arbitrary status values are blocked, but policy currently lives in service-level helper (not a full domain state-machine module)
   - **Recommended action:** keep current transition policy and evolve to centralized domain transition component if flows expand

3. **Courier status transitions are now validated (residual policy debt)**
   - **Severity:** low
   - **Where:** `CourierService.updateAssignedOrderStatus`
   - **Risk:** arbitrary status values are blocked, but transition semantics are still encoded in helper rules, not in richer workflow orchestration
   - **Recommended action:** keep current rules and extend only if new statuses/actors appear

4. **Order lifecycle state-machine remains partial**
   - **Severity:** medium
   - **Where:** broader order domain beyond restaurant/courier transition endpoints
   - **Risk:** while actor-specific transitions are now constrained, global lifecycle governance/history orchestration is still not fully unified across all order touchpoints
   - **Recommended action:** introduce full domain-level state-machine abstraction when scheduling next order-domain hardening iteration

5. **Null quantity in cart update is now handled defensively (residual validation debt)**
   - **Severity:** low
   - **Where:** `CartServiceImpl.updateItemQuantity(null)`
   - **Risk:** NPE path is removed via service-level guard; controller DTO validation and service boundary now both reject null quantity
   - **Recommended action:** keep defensive service boundary checks for critical fields and align future domain error catalog if centralized validation layer is introduced

6. **Backend runtime execution is prepared but not yet confirmed**
   - **Severity:** medium
   - **Where:** all backend tests in this cycle
   - **Risk:** repository is CI-ready, but until at least one successful backend runtime run is observed, hidden runtime/config issues may remain
   - **Recommended action:** run `./mvnw -Dspring.profiles.active=test test` in CI/local and move status to fully confirmed

## Minimal Changes Introduced

### Production code changes

1. **`AuthServiceImpl` DI fix**
   - Added missing `AppAccountJpaRepository` constructor injection/field
   - Purpose: compile/test correctness for existing `ensureUserAccount` usage

2. **`RestaurantMenuPage` error rendering fix**
   - Added error rendering in success state for add-to-cart failures
   - Purpose: surface backend/domain errors to users and make behavior testable

### Test-only infrastructure changes

1. **Frontend test stack setup**
   - Added Vitest/RTL dependencies and scripts
   - Added `vite` test config and `setupTests.js`

2. **Backend test DB setup**
   - Added test-scope H2 dependency
   - Added `src/test/resources/application-test.yml` for in-memory JPA tests (`flyway` disabled, `ddl-auto=create-drop`)

## Recommended Next Steps

1. **Confirm backend runtime in CI/local**
   - Execute Maven wrapper command and record first green run artifact/log

2. **Promote runtime risk to closed after confirmation**
   - Update this audit from "prepared/pending verification" to "confirmed"

3. **Final QA closure**
   - Freeze baseline for tests + mitigated risks and capture owners/priorities for residual debts
