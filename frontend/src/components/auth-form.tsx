"use client";

import { useGDP } from "@/lib/store";
import { useRouter } from "next/navigation";
import { useForm } from "react-hook-form";
import Link from "next/link";
import { Auth } from "@/lib/types";

import { Button } from "@/components/ui/button";
import {
    Card,
    CardContent,
    CardDescription,
    CardFooter,
    CardHeader,
    CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Separator } from "@/components/ui/separator";

interface AuthFormProps {
    mode: "login" | "register";
}

type AuthFormState = Auth;

export const AuthForm = ({ mode }: AuthFormProps) => {
    const router = useRouter();
    const { login, register: registerUser } = useGDP();
    const {
        register,
        handleSubmit,
        formState: { errors, isSubmitting },
        setError,
    } = useForm<AuthFormState>();

    const isLogin = mode === "login";

    const onSubmit = async (data: AuthFormState) => {
        try {
            if (isLogin) {
                await login({ email: data.email, password: data.password });
            } else {
                await registerUser({ pseudo: data.pseudo, email: data.email, password: data.password });
            }
            router.push("/dashboard");
        } catch (err: unknown) {
            setError("root", {
                message: err instanceof Error ? err.message : "Erreur serveur",
            });
        }
    };

    return (
        <Card style={{ width: "100%", maxWidth: "420px", margin: "0 auto" }}>
            <CardHeader>
                <CardTitle>{isLogin ? "Connexion" : "Créer un compte"}</CardTitle>
                <CardDescription>
                    {isLogin
                        ? "Entrez vos identifiants pour accéder à votre espace."
                        : "Remplissez le formulaire pour créer votre compte."}
                </CardDescription>
            </CardHeader>

            <form onSubmit={handleSubmit(onSubmit)}>
                <CardContent
                    style={{ display: "flex", flexDirection: "column", gap: "16px", margin: "16px 0" }}
                >
                    {!isLogin && (
                        <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                            <Label htmlFor="pseudo">Pseudo</Label>
                            <Input
                                id="pseudo"
                                type="text"
                                placeholder="Votre pseudo"
                                aria-invalid={!!errors.pseudo}
                                {...register("pseudo", { required: "Le pseudo est requis" })}
                            />
                            {errors.pseudo && (
                                <span style={{ fontSize: "12px", color: "hsl(var(--destructive))" }}>
                                    {errors.pseudo.message}
                                </span>
                            )}
                        </div>
                    )}

                    <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                        <Label htmlFor="email">Email</Label>
                        <Input
                            id="email"
                            type="email"
                            placeholder="exemple@domaine.com"
                            aria-invalid={!!errors.email}
                            {...register("email", {
                                required: "L'email est requis",
                                pattern: {
                                    value: /^[^\s@]+@[^\s@]+\.[^\s@]+$/,
                                    message: "Adresse email invalide",
                                },
                            })}
                        />
                        {errors.email && (
                            <span style={{ fontSize: "12px", color: "hsl(var(--destructive))" }}>
                                {errors.email.message}
                            </span>
                        )}
                    </div>

                    <div style={{ display: "flex", flexDirection: "column", gap: "6px" }}>
                        <Label htmlFor="password">Mot de passe</Label>
                        <Input
                            id="password"
                            type="password"
                            placeholder="••••••••"
                            aria-invalid={!!errors.password}
                            {...register("password", {
                                required: "Le mot de passe est requis",
                                minLength: {
                                    value: 8,
                                    message: "Le mot de passe doit contenir au moins 8 caractères",
                                },
                            })}
                        />
                        {errors.password && (
                            <span style={{ fontSize: "12px", color: "hsl(var(--destructive))" }}>
                                {errors.password.message}
                            </span>
                        )}
                    </div>

                    {errors.root && (
                        <Alert variant="destructive">
                            <AlertDescription>{errors.root.message}</AlertDescription>
                        </Alert>
                    )}
                </CardContent>

                <CardFooter
                    style={{ display: "flex", flexDirection: "column", gap: "12px" }}
                >
                    <Button type="submit" style={{ width: "100%" }} disabled={isSubmitting}>
                        {isSubmitting
                            ? "Chargement…"
                            : isLogin
                                ? "Se connecter"
                                : "S'inscrire"}
                    </Button>

                    <Separator />

                    <Button
                        variant="outline"
                        style={{ width: "100%" }}
                        render={<Link href={isLogin ? "/inscription" : "/connexion"} />}
                    >
                        {isLogin ? "Créer un compte" : "J'ai déjà un compte"}
                    </Button>
                </CardFooter>
            </form>
        </Card>
    );
};