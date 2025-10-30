import logoImage from '../assets/voto.jpg'
import './MiVotoLogo.css'

export default function MiVotoLogo({ className = '' }) {
  return (
    <div className={`mivoto-logo ${className}`}>
      <div className="mivoto-logo__accent" />

      <div className="mivoto-logo__inner">
        <img
          src={logoImage}
          alt="MiVoto logo"
          className="mivoto-logo__image"
          loading="lazy"
        />
      </div>
    </div>
  )
}
