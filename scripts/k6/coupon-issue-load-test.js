import http from 'k6/http';
import { Counter } from 'k6/metrics';

const successfulRequests = new Counter('successful_requests');
const quantityShortageResponses = new Counter('quantity_shortage_responses');
const duplicateIssueResponses = new Counter('duplicate_issue_responses');


export const options = {
    stages: [
        { duration: '1s', target: 300 },
        { duration: '2s', target: 500 },
        { duration: '2s', target: 100 },
    ],
    systemTags: ['status', 'http_req_duration', 'vus'],
    thresholds: {
        http_req_duration: ['p(95)<1000'],
    },
};

export default function () {
    const url = 'http://localhost:8080/api/v1/coupons/issue';

    const userId = Math.floor(Math.random() * 1500) + 1;

    const payload = JSON.stringify({
        userId: userId,
        couponId: 4
    });

    const params = {
        headers: {
            'Content-Type': 'application/json',
        },
    };

    const response = http.post(url, payload, params);

    if (response.status === 200) {
        successfulRequests.add(1);
    } else if (response.status === 409) {
        duplicateIssueResponses.add(1);
    } else if (response.status === 422) {
        quantityShortageResponses.add(1);
    } else {
        console.error(`Unexpected response status: ${response.status}, body: ${response.body}`);
    }
}