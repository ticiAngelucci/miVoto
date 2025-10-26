// SPDX-License-Identifier: MIT
pragma solidity ^0.8.0;

// Importar librerías de seguridad de OpenZeppelin
import "@openzeppelin/contracts/token/ERC721/ERC721.sol";
import "@openzeppelin/contracts/access/Ownable.sol";
import "@openzeppelin/contracts/security/ReentrancyGuard.sol"; // Mover import arriba

/**
 * @title SoulBoundTokenV2
 * @dev Token no transferible basado en ERC721.
 * @notice El emisor (Minter) debe ser el contrato de votación.
 */

contract SoulBoundTokenV2 is ERC721, Ownable {
    uint256 private _nextTokenId = 1;
    address public minter;

    constructor(address _initialMinter)
        ERC721("Soulbound Proof of Vote", "SBT-POV")
        Ownable(msg.sender)
    {
        minter = _initialMinter;
    }

    // Works in OZ v4.9.0+ (where `_beforeTokenTransfer` is `virtual`)
    function _beforeTokenTransfer(address from, address to, uint256 tokenId)
        internal
        virtual
    {
        require(from == address(0) || to == address(0), "SBT: Token no transferible.");
    }

    function safeMint(address to) public returns (uint256) {
        require(msg.sender == minter, "SBT: Solo el minter puede emitir tokens.");
        uint256 newTokenId = _nextTokenId++;
        _safeMint(to, newTokenId);
        return newTokenId;
    }

    function setMinter(address newMinter) public onlyOwner {
        require(newMinter != address(0), "Minter no puede ser la direccion cero.");
        minter = newMinter;
    }
}

// ---------------------------------------------------------------------------------

/**
 * @title VotacionConSBTv2
 * @dev Contrato de votación con SBT y protecciones de seguridad.
 */
contract VotacionConSBTv2 is Ownable, ReentrancyGuard {
    // Utilizamos 'SoulBoundTokenV2'
    SoulBoundTokenV2 public sbtContract;
    bool public isSBTVerified = false; // Añadido para la verificación de confianza

    struct Votante {
        bool haVotado;
        bool estaAutorizado;
    }

    enum Opcion { SI, NO, ABSTENCION }

    mapping(address => Votante) public votantes;
    mapping(Opcion => uint256) public resultados;

    event VotoEmitido(address indexed votante, Opcion voto);
    event VotanteAutorizado(address indexed votante);

    // CORRECCIÓN 3: Añadir Ownable(msg.sender)
    constructor(address _sbtAddress)
        Ownable(msg.sender)
    {
        sbtContract = SoulBoundTokenV2(_sbtAddress);
    }

    // Función de inicialización para la verificación de confianza (Opción A)
    /**
     * @notice Verifica y finaliza la conexión con el contrato SBT.
     * @dev Debe llamarse después de que el owner haya configurado el Minter en el SBT.
     */
    function finalizeSBTSetup() public onlyOwner {
        require(!isSBTVerified, "El SBT ya fue verificado.");
        
        // Verificar si el Minter del SBT es este contrato.
        // Si el SBT no tiene la dirección del Minter configurada (Paso 3 del despliegue), esta llamada fallará.
        require(sbtContract.minter() == address(this), "Este contrato no es el minter del SBT.");
        
        isSBTVerified = true;
    }
    
    /**
     * @notice Autoriza a una dirección a votar.
     * @param _votante La dirección que será autorizada.
     */
    function autorizarVotante(address _votante) public onlyOwner {
        votantes[_votante].estaAutorizado = true;
        emit VotanteAutorizado(_votante);
    }

    /**
     * @notice Permite a un votante autorizado emitir su voto y recibir un SBT.
     * @param _voto La opción de voto (0=SI, 1=NO, 2=ABSTENCION).
     */
    function votar(Opcion _voto) public nonReentrant {
        require(isSBTVerified, "La configuracion del SBT no ha finalizado."); // Requisito de verificación
        require(uint256(_voto) <= uint256(Opcion.ABSTENCION), "Voto invalido.");
        
        require(votantes[msg.sender].estaAutorizado, "Votante no autorizado.");
        require(!votantes[msg.sender].haVotado, "Ya has emitido tu voto.");

        // Checks-Effects-Interactions 
        votantes[msg.sender].haVotado = true;
        resultados[_voto]++;
        emit VotoEmitido(msg.sender, _voto);
        
        sbtContract.safeMint(msg.sender); 
    }

    /**
     * @notice Devuelve el total de votos para una opción específica.
     * @param _opcion La opción a consultar.
     */
    function obtenerResultados(Opcion _opcion) public view returns (uint256) {
        return resultados[_opcion];
    }
}
