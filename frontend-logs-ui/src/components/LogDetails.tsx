import { BackendLog } from '../types'
import FlexibleButton from './FlexibleButton'

interface Props {
    log: BackendLog
    onBack: () => void
}

export default function LogDetails({ log, onBack }: Props) {
    return (
        <div className="bg-white shadow-xl rounded-2xl overflow-hidden text-gray-600 p-4 space-y-6">
            {/* Кнопка назад */}
            <div>
                <FlexibleButton
                    onClick={onBack}
                    color="bg-gray-200 hover:bg-gray-300 text-gray-700"
                >
                    ← Назад к списку
                </FlexibleButton>
            </div>

            {/* Основная информация */}
            <div className="grid grid-cols-[120px_1fr] gap-4 items-start">
                <div className="font-medium text-gray-500">Время</div>
                <div>{log.logTs}</div>

                <div className="font-medium text-gray-500">Уровень</div>
                <div className={getLevelClass(log.logLevel)}>{log.logLevel}</div>

                <div className="font-medium text-gray-500">Logger</div>
                <div className="font-mono text-sm">{log.logger}</div>

                {log.instance && (
                    <>
                        <div className="font-medium text-gray-500">Instance</div>
                        <div>{log.instance}</div>
                    </>
                )}

                {log.appVersion && (
                    <>
                        <div className="font-medium text-gray-500">Версия</div>
                        <div>{log.appVersion}</div>
                    </>
                )}

                {log.userLogin && (
                    <>
                        <div className="font-medium text-gray-500">Пользователь</div>
                        <div>{log.userLogin}</div>
                    </>
                )}
            </div>

            {/* Сообщение */}
            <div className="space-y-2">
                <div className="font-medium text-gray-500">Сообщение</div>
                <pre className="whitespace-pre-wrap font-mono text-sm bg-gray-50 p-4 rounded-xl">
                    {log.logMessage}
                </pre>
            </div>

            {/* Exception */}
            {(log.logException || log.logStack) && (
                <div className="space-y-2">
                    <div className="font-medium text-gray-500">Exception</div>
                    <pre className="whitespace-pre-wrap font-mono text-sm bg-red-50 text-red-700 p-4 rounded-xl">
                        {log.logException}
                        {log.logStack && '\n\n' + log.logStack}
                    </pre>
                </div>
            )}

            {/* MDC */}
            {log.mdc && (
                <div className="space-y-2">
                    <div className="font-medium text-gray-500">MDC</div>
                    <pre className="whitespace-pre-wrap font-mono text-sm bg-gray-50 p-4 rounded-xl">
                        {log.mdc}
                    </pre>
                </div>
            )}
        </div>
    )
}

function getLevelClass(level: string | undefined) {
    switch ((level || '').toUpperCase()) {
        case 'ERROR':
            return 'text-red-500 font-medium'
        case 'WARN':
            return 'text-yellow-500 font-medium'
        case 'INFO':
            return 'text-gray-400 font-medium'
        case 'DEBUG':
            return 'text-blue-700 font-medium'
        default:
            return 'text-gray-500 font-medium'
    }
}