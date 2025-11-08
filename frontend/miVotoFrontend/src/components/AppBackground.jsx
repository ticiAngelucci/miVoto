import { useMemo } from 'react'
import '../App.css'

const useBackgroundLayers = () =>
  useMemo(
    () => (
      <>
        <div className="background-gradients">
          <div className="gradient gradient--one" />
          <div className="gradient gradient--two" />
          <div className="gradient gradient--three" />
        </div>
        <div className="login-bottom-arc" />
      </>
    ),
    []
  )

function AppBackground({ children, className = '' }) {
  const layers = useBackgroundLayers()
  const wrapperClassName = ['app-background', className].filter(Boolean).join(' ')

  return (
    <div className={wrapperClassName}>
      {layers}
      {children}
    </div>
  )
}

export default AppBackground

export { useBackgroundLayers }
