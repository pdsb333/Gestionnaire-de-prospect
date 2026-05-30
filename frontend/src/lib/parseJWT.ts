export function parseJwt(token: string): { exp: number } | null {
    try {
        const payload = token.split('.')[1];
        const decoded = JSON.parse(Buffer.from(payload, 'base64').toString('utf-8'));
        return decoded;
    } catch (err) {
        return null;
    }
}
