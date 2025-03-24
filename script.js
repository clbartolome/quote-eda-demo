import http from 'k6/http';

  
export const options = {
  vus: 50,
  duration: '10s',
};

export default function () {
  const url = 'https://quote-producer-quotes.apps.hetzner.calopezb.com/quotes/request';
  
  const payload = JSON.stringify({});

  const params = {
    headers: {
      'Content-Type': 'application/json',
    },
  };

  http.post(url, payload, params);
}
