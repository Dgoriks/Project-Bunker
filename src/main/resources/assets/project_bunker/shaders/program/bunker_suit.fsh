#version 150

in vec2 texCoord;
out vec4 fragColor;

uniform sampler2D DiffuseSampler;
uniform float Time;

void main() {
    vec2 uv = texCoord;

    // Считываем оригинальный чистый пиксель игры (сохраняет 100% настоящих цветов)
    vec4 baseColor = texture(DiffuseSampler, uv);

    // 1. АТМОСФЕРНАЯ НЕОНОВАЯ ВИНИЕТКА (Свечение шлема по краям)
    // Вычисляем расстояние от центра экрана
    float distFromCenter = length(uv - 0.5);
    // Создаем плавное затемнение и подкрашивание, которое усиливается только к углам монитора
    float glowMask = smoothstep(0.4, 0.75, distFromCenter);

    // Задаем цвет неонового интерфейса (мягкий зеленый)
    vec3 hudGlowColor = vec3(0.0, 0.8, 0.2);

    // Плавно подмешиваем зеленое свечение по краям экрана, не трогая центр обзора
    vec3 finalColor = mix(baseColor.rgb, mix(baseColor.rgb, hudGlowColor, 0.25), glowMask);

    // 2. УЛЬТРА-ТОНКАЯ ЦИФРОВАЯ РЯБЬ (Эффект работающей матрицы костюма)
    // Сделали частоту очень высокой (3000.0), а силу — ничтожно малой (0.005), чтобы она не била по глазам
    float scanline = sin(uv.y * 3000.0 + Time * 8.0) * 0.005;
    finalColor += scanline;

    // 3. МИКРО-ШУМ ДЕТЕКТОРА (Реалистичные помехи)
    // Коэффициент уменьшен до 0.006 — шум станет едва уловимым, создавая ощущение стекла шлема
    float noise = (fract(sin(dot(uv, vec2(12.9898, 78.233))) * 43758.5453)) * 0.006;
    finalColor += noise;

    fragColor = vec4(finalColor, 1.0);
}