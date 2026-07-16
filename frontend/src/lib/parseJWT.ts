export function parseJwt(token: string): { exp: number } | null {
    try {
        const payload = token.split('.')[1];
        const decoded = JSON.parse(Buffer.from(payload, 'base64').toString('utf-8'));
        if (typeof decoded !== "object" || decoded === null || typeof decoded.exp !== "number") {
            return null;
        }
        return decoded;
    } catch (err) {
        return null;
    }
}
