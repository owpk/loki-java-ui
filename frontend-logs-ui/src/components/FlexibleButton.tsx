import { FC, MouseEvent, useState } from 'react';

// Определяем кастомные пропсы, а затем объединяем их с нативными пропсами кнопки
type FlexibleButtonProps = {
  color?: string; // Кастомный пропс для цвета
} & React.ComponentProps<'button'>;


const FlexibleButton: FC<FlexibleButtonProps> = ({
  children, // Извлекаем children, чтобы явно его использовать
  color = 'bg-blue-500 hover:bg-blue-600',
  className = '', // Добавляем пустую строку по умолчанию для объединения классов
  ...rest // Собираем все остальные пропсы
}) => {

    const [effect, setEffect] = useState(false);

    const handleClick = (e: MouseEvent<HTMLButtonElement>) => {
        setEffect(true);
        rest.onClick?.(e);
    };

    const handleAnimationEnd = () => {
        setEffect(false);
    };

    return (
        <button
            className={`
        ${color} 
        rounded-2xl px-4 py-2 rounded 
        font-medium 
        text-white
        disabled:bg-gray-200 disabled:cursor-not-allowed 
        transition-colors duration-200
        ${effect && 'animate-wiggle'}
      `}
            onClick={handleClick}
            onAnimationEnd={handleAnimationEnd}
            {...rest}
        >
           {children}
        </button>
    );
};

export default FlexibleButton;