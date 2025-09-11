import http from 'k6/http';
import { Counter, Trend } from 'k6/metrics';
import { check, sleep } from 'k6';

const successfulRequests = new Counter('successful_requests');
const failedRequests = new Counter('failed_requests');
const popularProductsRequests = new Counter('popular_products_requests');
const specificProductRequests = new Counter('specific_product_requests');
const pointChargeRequests = new Counter('point_charge_requests');
const orderRequests = new Counter('order_requests');

const popularProductsDuration = new Trend('popular_products_duration');
const specificProductDuration = new Trend('specific_product_duration');
const pointChargeDuration = new Trend('point_charge_duration');
const orderDuration = new Trend('order_duration');

export const options = {
    stages: [
        { duration: '30s', target: 30 },
        { duration: '1m', target: 75 },
        { duration: '1m', target: 100 },
        { duration: '1m', target: 75 },
        { duration: '30s', target: 30 },
    ],
    systemTags: ['status', 'http_req_duration', 'vus'],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
        http_req_failed: ['rate<0.001'],
    },
};

const BASE_URL = 'http://localhost:8080';

export default function () {
    const userId = Math.floor(Math.random() * 1000) + 1;
    const productId = 3;
    
    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    // Step 1: Get popular products
    const popularProductsResponse = http.get(`${BASE_URL}/api/v1/products/popular`, params);
    
    popularProductsRequests.add(1);
    popularProductsDuration.add(popularProductsResponse.timings.duration);
    
    const popularProductsCheck = check(popularProductsResponse, {
        'Popular products status is 200': (r) => r.status === 200,
        'Popular products response time < 2000ms': (r) => r.timings.duration < 2000,
    });
    
    if (!popularProductsCheck) {
        failedRequests.add(1);
        return;
    }

    sleep(0.5);

    // Step 2: Get specific product (productId = 3)
    const specificProductResponse = http.get(`${BASE_URL}/api/v1/products/${productId}`, params);
    
    specificProductRequests.add(1);
    specificProductDuration.add(specificProductResponse.timings.duration);
    
    const specificProductCheck = check(specificProductResponse, {
        'Specific product status is 200': (r) => r.status === 200,
        'Specific product response time < 2000ms': (r) => r.timings.duration < 2000,
    });
    
    if (!specificProductCheck) {
        failedRequests.add(1);
        return;
    }

    sleep(0.5);

    // Step 3: Charge points (1000 points)
    const chargePointPayload = JSON.stringify({
        amount: 1000
    });
    
    const chargePointResponse = http.post(`${BASE_URL}/api/v1/users/${userId}/points`, chargePointPayload, params);
    
    pointChargeRequests.add(1);
    pointChargeDuration.add(chargePointResponse.timings.duration);
    
    const chargePointCheck = check(chargePointResponse, {
        'Point charge status is 200': (r) => r.status === 200,
        'Point charge response time < 2000ms': (r) => r.timings.duration < 2000,
    });
    
    if (!chargePointCheck) {
        failedRequests.add(1);
        return;
    }

    sleep(0.5);

    // Step 4: Place order/payment
    const orderPayload = JSON.stringify({
        userId: userId,
        userCouponId: null,
        orderItems: [
            {
                productId: productId,
                quantity: 1
            }
        ]
    });
    
    const orderResponse = http.post(`${BASE_URL}/api/v1/orders`, orderPayload, params);
    
    orderRequests.add(1);
    orderDuration.add(orderResponse.timings.duration);
    
    const orderCheck = check(orderResponse, {
        'Order status is 200': (r) => r.status === 200,
        'Order response time < 3000ms': (r) => r.timings.duration < 3000,
    });
    
    if (orderCheck) {
        successfulRequests.add(1);
    } else {
        failedRequests.add(1);
    }

}