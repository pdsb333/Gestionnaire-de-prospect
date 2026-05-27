"use client";

import { AuthForm } from "@/components/auth-form";

export default function ConnexionPage() {
    return (
        <div className="w-screen h-screen flex items-center justify-center">
            <AuthForm mode="register" />
        </div>
    );
}
